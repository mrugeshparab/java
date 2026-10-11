$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$passwords = @{
    'student@campusnest.test' = 'CampusNestStudent2026!'
    'owner@campusnest.test' = 'CampusNestOwner2026!'
}

foreach ($entry in @(
    @{ email = 'student@campusnest.test'; name = 'CampusNest Student'; role = 'USER'; password = $passwords['student@campusnest.test'] },
    @{ email = 'owner@campusnest.test'; name = 'CampusNest Property Owner'; role = 'OWNER'; password = $passwords['owner@campusnest.test'] }
)) {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $csrf = Invoke-RestMethod "$base/api/auth/csrf" -WebSession $session
    $registration = @{ fullName = $entry.name; email = $entry.email; password = $entry.password; phone = ''; role = $entry.role } | ConvertTo-Json
    Invoke-RestMethod "$base/api/auth/register" -Method Post -WebSession $session -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -ContentType 'application/json' -Body $registration | Out-Null
}

$ownerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrf = Invoke-RestMethod "$base/api/auth/csrf" -WebSession $ownerSession
$ownerLogin = @{ email = 'owner@campusnest.test'; password = $passwords['owner@campusnest.test'] } | ConvertTo-Json
Invoke-RestMethod "$base/api/auth/login" -Method Post -WebSession $ownerSession -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -ContentType 'application/json' -Body $ownerLogin | Out-Null
$csrf = Invoke-RestMethod "$base/api/auth/csrf" -WebSession $ownerSession
$listing = @{ title = 'room'; description = 'd'; propertyType = 'PG'; locality = 'panvel'; city = 'kamothe'; monthlyRent = 12000; furnishing = 'yes' } | ConvertTo-Json
$property = Invoke-RestMethod "$base/api/owner/properties" -Method Post -WebSession $ownerSession -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -ContentType 'application/json' -Body $listing
$photoPath = Join-Path $env:TEMP 'campusnest-owner-home-photo.png'
$uploads = Invoke-RestMethod "$base/api/owner/properties/$($property.id)/images" -Method Post -WebSession $ownerSession -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -Form @{ images = Get-Item -LiteralPath $photoPath }

$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrf = Invoke-RestMethod "$base/api/auth/csrf" -WebSession $adminSession
$adminLogin = @{ email = 'admin@campusnest.test'; password = 'CampusNestLocal2026!' } | ConvertTo-Json
Invoke-RestMethod "$base/api/auth/login" -Method Post -WebSession $adminSession -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -ContentType 'application/json' -Body $adminLogin | Out-Null
$csrf = Invoke-RestMethod "$base/api/auth/csrf" -WebSession $adminSession
Invoke-RestMethod "$base/api/admin/properties/$($property.id)/approve" -Method Patch -WebSession $adminSession -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } | Out-Null

$studentSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrf = Invoke-RestMethod "$base/api/auth/csrf" -WebSession $studentSession
$studentLogin = @{ email = 'student@campusnest.test'; password = $passwords['student@campusnest.test'] } | ConvertTo-Json
Invoke-RestMethod "$base/api/auth/login" -Method Post -WebSession $studentSession -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -ContentType 'application/json' -Body $studentLogin | Out-Null
$details = Invoke-RestMethod "$base/api/user/properties/$($property.id)" -WebSession $studentSession
"Restored approved home '$($details.title)' with $($details.images.Count) photo(s)."
"Student contact email: $($details.ownerEmail)"
