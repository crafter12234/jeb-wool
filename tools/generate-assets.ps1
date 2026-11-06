# =====================================================================
#  generate-assets.ps1 - создаёт текстуры и модели мода "jeb_ Wool"
#
#  Берёт НАСТОЯЩИЕ текстуры игры из вашего minecraft-client.jar, склеивает
#  16 цветов в анимированную полосу (с плавным переходом между цветами)
#  и копирует ванильные модели кровати.
#
#  Запуск (из любой папки):
#     powershell -ExecutionPolicy Bypass -File tools\generate-assets.ps1
#  Если jar не найден сам, укажите путь:
#     powershell -ExecutionPolicy Bypass -File tools\generate-assets.ps1 -Jar "C:\путь\minecraft-client.jar"
# =====================================================================
param([string]$Jar = "")

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem

$ModId     = "jeb_wool"
$FrameTime = 25    # сколько тиков держится один цвет (как у овцы jeb_)
# Порядок цветов такой же, как у овцы jeb_ (порядок DyeColor).
$Colors = @("white","orange","magenta","light_blue","yellow","lime","pink","gray",
            "light_gray","cyan","purple","blue","brown","green","red","black")

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$AssetsOut   = Join-Path $ProjectRoot ("src\main\resources\assets\" + $ModId)

# ---- 1. Ищем minecraft-client.jar ----------------------------------------
if (-not $Jar) {
    $cache = Join-Path $env:USERPROFILE ".gradle\caches\fabric-loom"
    $found = Get-ChildItem $cache -Recurse -Filter "minecraft-client.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $found) {
        throw "minecraft-client.jar не найден. Сначала выполните gradlew build, либо укажите путь через -Jar."
    }
    $Jar = $found.FullName
}
Write-Host "Читаю: $Jar"
$zip  = [IO.Compression.ZipFile]::OpenRead($Jar)
$utf8 = New-Object Text.UTF8Encoding($false)   # UTF-8 без BOM

$script:doneTextures = @{}
$script:doneModels   = @{}
$script:warnings     = New-Object Collections.Generic.List[string]

# ---- 2. Вспомогательные функции -----------------------------------------
function Save-Text([string]$path, [string]$text) {
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $path) | Out-Null
    [IO.File]::WriteAllText($path, $text, $utf8)
}

function Read-Text([string]$entryPath) {
    $e = $zip.GetEntry($entryPath)
    if ($null -eq $e) { return $null }
    $reader = New-Object IO.StreamReader($e.Open(), [Text.Encoding]::UTF8)
    try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}

function Read-Image([string]$entryPath) {
    $e = $zip.GetEntry($entryPath)
    if ($null -eq $e) { return $null }
    $ms = New-Object IO.MemoryStream
    $s = $e.Open(); $s.CopyTo($ms); $s.Dispose()
    $ms.Position = 0
    $img = [Drawing.Image]::FromStream($ms)
    $bmp = New-Object Drawing.Bitmap($img)   # копия, не зависящая от потока
    $img.Dispose(); $ms.Dispose()
    return $bmp
}

# Склеивает 16 текстур (white_X, orange_X, ...) в одну вертикальную полосу + анимация.
function New-ColorStrip([string]$kind, [string]$name) {
    $key = "$kind/$name"
    if ($script:doneTextures.ContainsKey($key)) { return }
    $script:doneTextures[$key] = $true

    $newName = $name.Replace("white", "jeb")
    $frames = @()
    foreach ($c in $Colors) {
        $p = "assets/minecraft/textures/$kind/" + $name.Replace("white", $c) + ".png"
        $img = Read-Image $p
        if ($null -eq $img) {
            $script:warnings.Add("Не найдена текстура: $p")
            foreach ($f in $frames) { $f.Dispose() }
            return
        }
        $frames += $img
    }

    $w = $frames[0].Width
    $h = $frames[0].Height
    $strip = New-Object Drawing.Bitmap($w, ($h * $frames.Count), [Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [Drawing.Graphics]::FromImage($strip)
    $g.CompositingMode   = [Drawing.Drawing2D.CompositingMode]::SourceCopy
    $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.PixelOffsetMode   = [Drawing.Drawing2D.PixelOffsetMode]::Half
    for ($i = 0; $i -lt $frames.Count; $i++) {
        $g.DrawImage($frames[$i], 0, ($i * $h), $w, $h)
    }
    $g.Dispose()

    $dest = Join-Path $AssetsOut ("textures\$kind\" + $newName + ".png")
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $dest) | Out-Null
    $strip.Save($dest, [Drawing.Imaging.ImageFormat]::Png)
    $strip.Dispose()
    foreach ($f in $frames) { $f.Dispose() }

    # interpolate = плавный переход между соседними цветами
    $meta = '{"animation":{"frametime":' + $FrameTime + ',"interpolate":true,"width":' + $w + ',"height":' + $h + '}}'
    Save-Text ($dest + ".mcmeta") $meta
    Write-Host ("  текстура: {0}/{1}  ({2}x{3}, 16 кадров)" -f $kind, $newName, $w, $h)
}

# Копирует ванильную модель, переписывая ссылки на "белые" файлы в наши "jeb_".
function Copy-Model([string]$kind, [string]$name) {
    $key = "$kind/$name"
    if ($script:doneModels.ContainsKey($key)) { return }
    $script:doneModels[$key] = $true

    $text = Read-Text "assets/minecraft/models/$kind/$name.json"
    if ($null -eq $text) { $script:warnings.Add("Не найдена модель: models/$kind/$name.json"); return }
    $newText = Convert-References $text
    Save-Text (Join-Path $AssetsOut ("models\$kind\" + $name.Replace("white", "jeb") + ".json")) $newText
    Write-Host ("  модель:   {0}/{1}" -f $kind, $name.Replace("white", "jeb"))
}

# Находит в JSON ссылки вида "minecraft:block/white_..." и заменяет их на наши.
# Ключи model/parent/base - это модели (копируем), остальные - текстуры (склеиваем).
function Convert-References([string]$text) {
    $pattern = '"(?<key>[A-Za-z_0-9]+)"\s*:\s*"(?<ns>minecraft:)?(?<kind>block|item)/(?<name>[^"#]+)"'
    $evaluator = [Text.RegularExpressions.MatchEvaluator]{
        param($m)
        $key  = $m.Groups["key"].Value
        $kind = $m.Groups["kind"].Value
        $name = $m.Groups["name"].Value
        if (-not $name.Contains("white")) { return $m.Value }   # не зависит от цвета - оставляем
        if (@("model", "parent", "base") -contains $key) { Copy-Model $kind $name }
        else { New-ColorStrip $kind $name }
        return '"' + $key + '": "' + $ModId + ':' + $kind + '/' + $name.Replace("white", "jeb") + '"'
    }
    return [regex]::Replace($text, $pattern, $evaluator)
}

# ---- 3. Шерсть (она же текстура ковра, плиты и ступенек) -----------------
Write-Host "Шерсть..."
New-ColorStrip "block" "white_wool"

# ---- 4. Иконка подушки ---------------------------------------------------
Write-Host "Подушка..."
New-ColorStrip "item" "white_cushion"

# ---- 5. Кровать: копируем ванильные blockstate, предмет и модели ---------
Write-Host "Кровать..."
$bedOk = $true
foreach ($folder in @("blockstates", "items")) {
    $text = Read-Text "assets/minecraft/$folder/white_bed.json"
    if ($null -eq $text) { $bedOk = $false; $script:warnings.Add("Не найден assets/minecraft/$folder/white_bed.json"); continue }
    Save-Text (Join-Path $AssetsOut "$folder\jeb_bed.json") (Convert-References $text)
    Write-Host "  $folder/jeb_bed.json"
}
if (-not $bedOk) {
    Write-Host "Файлы кроватей в вашей версии называются иначе. Вот что есть:"
    $zip.Entries | Where-Object { $_.FullName -match "assets/minecraft/(blockstates|items|models/block|models/item)/.*bed" } |
        Select-Object -First 40 -ExpandProperty FullName
}

$zip.Dispose()

# ---- 6. Итог --------------------------------------------------------------
Write-Host ""
if ($script:warnings.Count -gt 0) {
    Write-Host "ПРЕДУПРЕЖДЕНИЯ:" -ForegroundColor Yellow
    $script:warnings | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
    Write-Host "Пришлите этот вывод - я поправлю скрипт."
} else {
    Write-Host "Готово без ошибок. Теперь выполните: ./gradlew build" -ForegroundColor Green
}
