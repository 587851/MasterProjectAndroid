# MasterProjectAndroid

En Android-app som leser helsedata via **Health Connect**, mapper dem til **FHIR R4 Observation**-ressurser og laster dem opp til en ekstern FHIR-server.
Appen sporer synkroniserte records lokalt (for å unngå duplikater), og kjører automatisk synkronisering i bakgrunnen via **WorkManager**.
UI er bygget i **Jetpack Compose**.

---

## Innhold

* [Skjermbilder](#skjermbilder)
* [Funksjoner](#funksjoner)
* [Arkitektur & flyt](#arkitektur--flyt)
* [Arkitektur og prinsipper (SOLID + MVVM)](#arkitektur-og-prinsipper-solid--mvvm)
* [Teknologier](#teknologier)
* [Prosjektstruktur](#prosjektstruktur)
* [Oppsett](#oppsett)
* [Bruk](#bruk)
* [Automatisk synk](#automatisk-synk)
* [Data & persistens](#data--persistens)
* [Tilganger (Health Connect)](#tilganger-health-connect)
* [Feilsøking](#feilsøking)
* [Videre arbeid / TODO](#videre-arbeid--todo)

---

## Skjermbilder

### Hovedskjerm

### Hovedskjerm

### Innstillinger

### Historikk

### Tillatelser


## Funksjoner

* **Les helsedata** fra Health Connect (f.eks. Heart Rate, Steps, Sleep, VO₂ Max, m.fl.)
* **Mapper** HC-records til FHIR `Observation`-ressurser
* **Laster opp** til ekstern FHIR-server (batch/transaksjon)
* **Duplikatkontroll**: sporer hvilke HC-records som er synket
* **Automatisk synk** via WorkManager (15 min / time / dag / uke / måned)
* **Historikk**: viser når/hva som ble sendt
* **Tillatelser**: ber om og viser Health Connect-tilganger

---

## Arkitektur & flyt

```
Health Connect --> HealthDataReader ------+
                                           \
                                            --> RecordMapper --> FHIR Observations --> FHIRUploader --> FHIR Server
                                           /
SyncedRecordRepository <--- RecordSyncer <-+
          ^                                       |
          |                                       v
          +-------- ObservationUploader (chunking, sync tracking)

Room DB (HistoryRecord, SyncedRecord) <--- Historikk & duplikatkontroll

WorkManager (AutoSyncWorker) --> kjører lesing + opplasting periodisk

UI (Jetpack Compose):
- MainScreen: lese/tegne/eksportere data
- Settings: prefs, auto-sync, pasientinfo
- Permissions: status + forespørsler
- History: gruppert visning av tidligere synk
```

**DI / Composition root:** `DependencyProvider` oppretter og deler klienter, repoer, mapper/uploader, osv.
**App oppstart:** `MainApplication` initierer WorkManager med custom `AutoSyncWorkerFactory`.

---

## Arkitektur og prinsipper (SOLID + MVVM)

Prosjektet er bygget etter **MVVM-arkitektur** og følger **SOLID-prinsippene** for god struktur, testbarhet og vedlikeholdbarhet.

### MVVM (Model–View–ViewModel)

* **Model**
  Datakilder og logikk — inkluderer Room-databasen (`AppDatabase`), repository-lag (`IHistoryRecordRepository`, `ISyncedRecordRepository`), preferanser (`SyncPreferences`, `PatientPreferences`) og FHIR-integrasjonen (lesing og opplasting av helsedata).

* **ViewModel**
  Forretningslogikk og dataflyt mellom Model og UI.
  Alle ViewModel-ene eksponerer **StateFlow** og **UiEvent**-strømmer for Compose-UI:

  * `MainViewModel` – datahenting, sending og visningsmodus
  * `SettingsViewModel` – synk-innstillinger
  * `PermissionsViewModel` – Health Connect-status og tillatelser
  * `HistoryViewModel` – gruppering av tidligere synk
  * `PatientViewModel` – håndtering av pasientinfo
  * `StartupViewModel` – rydder gamle records ved oppstart

* **View (UI)**
  Bygget i **Jetpack Compose** – reaktivt, deklarativt og direkte koblet til `StateFlow`-verdier fra ViewModel-ene.
  Skjermene (`MainScreen`, `SettingsScreen`, `HistoryScreen`, `PermissionsScreen`) er enkle og uten logikk — alt håndteres i ViewModel.

---

### SOLID-prinsippene i praksis

| Prinsipp                  | Hvordan det brukes                                                                                                                                                                          |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **S**ingle Responsibility | Hver klasse gjør én ting: `AutoSyncWorker` kun synk, `HistoryRecordRepository` kun databaseoperasjoner, `FhirPatientManager` kun håndtering av `Patient`.                                   |
| **O**pen/Closed           | Nye datatyper og FHIR-mappinger kan legges til uten å endre eksisterende logikk — ved å utvide `ObservationType` og `ObservationUploader`.                                                  |
| **L**iskov Substitution   | Repository-interfacer (`IHistoryRecordRepository`, `ISyncedRecordRepository`) gjør at implementasjoner kan byttes uten å bryte brukerne (for eksempel ved testing).                         |
| **I**nterface Segregation | Små og fokuserte interfacer i `preferences.interfaces` og `repositories.interfaces`. Ingen klasse er tvunget til å implementere metoder den ikke trenger.                                   |
| **D**ependency Inversion  | Høyere nivå (ViewModel-er og Workers) avhenger av **abstraksjoner** (interfacer) i stedet for konkrete implementasjoner. Disse leveres via `DependencyProvider` og `AutoSyncWorkerFactory`. |

---

### Avhengighetsinjeksjon

Appen bruker en enkel **DependencyProvider** (manuell DI) som samler alle nødvendige singletons (f.eks. `HealthConnectClient`, `FHIRUploader`, repositories, preferences osv.) og leverer disse til ViewModels og WorkManager-factory.
Dette gjør appen:

* mer **modulær**
* lettere å **teste**
* og uavhengig av Android-livssyklus for opprettelse av objekter.

---

## Teknologier

* **Kotlin**, **Jetpack Compose**, **Material 3**
* **Health Connect** (lese helsedata + bakgrunnslesing)
* **HAPI FHIR** (R4) – `FhirContext`, `IGenericClient`, `Observation`, `Patient`
* **Room** (lokal DB) – `HistoryRecord`, `SyncedRecord`
* **DataStore** – pasient- og sync-preferanser
* **WorkManager** – periodisk synk i bakgrunnen
* **Coroutines & Flow** – asynkron databehandling og reaktiv UI

---

## Prosjektstruktur

```
com.example.masterprojectandroid
├── appdatabase/
│   └── AppDatabase.kt
├── dao/
│   ├── HistoryRecordDao.kt
│   └── SyncedRecordDao.kt
├── di/
│   └── DependencyProvider.kt
├── entities/
│   ├── HistoryRecord.kt
│   └── SyncedRecord.kt
├── enums/
│   ├── HealthConnectAvailability.kt
│   ├── ObservationType.kt
│   └── Screen.kt
├── fhir/
│   ├── upload/
│   │   ├── FHIRUploader.kt
│   │   ├── FhirPatientManager.kt
│   │   └── ObservationUploader.kt
│   └── utils/
│       └── RecordSyncer.kt
├── healthconnect/
│   ├── data/HealthDataReader.kt
│   ├── services/
│   │   ├── HealthConnectClientProvider.kt
│   │   ├── HealthConnectStatusService.kt
│   │   └── HealthPermissionManager.kt
│   └── utils/
│       ├── HealthDataFormatter.kt
│       ├── HealthDataPoint.kt
│       └── (konstanter & permission-lister)
├── models/
│   ├── PatientInfo.kt
│   └── PermissionInfo.kt
├── preferences/
│   ├── PatientPreferences.kt
│   └── SyncPreferences.kt
├── repositories/
│   ├── implementations/
│   │   ├── HistoryRecordRepository.kt
│   │   └── SyncedRecordRepository.kt
│   └── interfaces/
├── synchronization/
│   ├── AutoSyncWorker.kt
│   ├── AutoSyncWorkerFactory.kt
│   └── AutoSyncWorkerScheduler.kt
├── ui/
│   ├── components/
│   └── screens/
├── viewmodels/
│   ├── MainViewModel.kt
│   ├── SettingsViewModel.kt
│   ├── PermissionsViewModel.kt
│   ├── PatientViewModel.kt
│   ├── HistoryViewModel.kt
│   └── StartupViewModel.kt
├── MainActivity.kt
└── MainApplication.kt
```

## Oppsett

### 1) Krav

* Android Studio (Giraffe/opp) og JDK 17+
* Android 8.1 (API 27, `O_MR1`) eller nyere (Health Connect minstekrav i prosjektet)
* En FHIR R4 server-URL

### 2) Konfigurer FHIR server-URL

Prosjektet forventer `BuildConfig.FHIR_SERVER_URL`. Legg den i `build.gradle(.kts)` for `app` eller via `local.properties`/Gradle `buildConfigField`:

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "FHIR_SERVER_URL", "\"https://your-fhir-server/baseR4\"")
    }
}
```

### 3) Avhengigheter

Sørg for at du har libraries for:

* Health Connect
* HAPI FHIR (R4)
* Room (runtime, ktx, compiler)
* WorkManager (ktx)
* DataStore (preferences)
* Compose (Material3, tooling, navigation)


### 4) App-klasse

`AndroidManifest.xml` må peke på `MainApplication`:

```xml
<application
    android:name=".MainApplication"
    ... >
</application>
```

---

## Bruk

1. **Start appen** – første gang vil `StartupViewModel` rydde gamle `SyncedRecord`-rader ift. preferanser.
2. **Gå til “Permissions”** – be om nødvendige Health Connect-tillatelser.
3. **Main**:

   * Velg *Data Type* og *Time Range*
   * Klikk **Read Data** for å hente og se data (tekst/bar/graf)
   * Klikk **Send data to server** for å mappe → lage FHIR `Observation` → sende
4. **History** – se historikk over hva som ble sendt (type, antall, periode, kilde).
5. **Settings** – sett auto-sync, cleanup, duplikater og pasientnavn.

---

## Automatisk synk

* **Planlegging:** `AutoSyncWorkerScheduler` planlegger `AutoSyncWorker` med intervaller:

  * 15 min, 1 time, 1 dag, 1 uke, 1 måned
* **Krav:** Nettverk (`CONNECTED`)
* **Bakgrunnslesing:** Krever at funksjonen er tilgjengelig og at tillatelsen
  `PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND` er innvilget (sjekkes i `SettingsViewModel`).
* **Vindu:** `AutoSyncWorker` leser data for et definert tidsvindu (f.eks. siste 30 min / 2 timer / 2 dager / 2 uker / 2 måneder) avhengig av frekvensen – for å sikre at etterslep fanges opp.

---

## Data & persistens

* **Room**

  * `HistoryRecord` – historikk over manuell/automatisk opplasting
  * `SyncedRecord` – hvilke HC-record IDs er allerede synket (for duplikatkontroll)
* **DataStore**

  * `SyncPreferences` – duplikater, cleanup, frekvens, auto-sync-typer
  * `PatientPreferences` – pasientnavn og `patientId` (fra FHIR-serveren)
* **FHIR**

  * `FhirPatientManager` – finner/lagrer/lagrer pasient-ID; oppretter `Patient` ved behov
  * `FHIRUploader` – sender `Observation`-bundles som transaksjoner
  * `ObservationUploader` – filtrerer duplikater, mapper, chunker (500), sender, og markerer som synket

---

## Tilganger (Health Connect)

Appen bruker en lang liste lese-tillatelser (bl.a. Heart Rate, Steps, Sleep, osv.), samt:

* **Background tasks**: `PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND`
* **Read data older than 30 days**: `PERMISSION_READ_HEALTH_DATA_HISTORY`

**PermissionsScreen** viser status og lar deg be om tilganger.
**HealthPermissionManager** håndterer per-type og bulk-forespørsel.

---

## Feilsøking

* **Ingen data vises**

  * Sjekk at Health Connect er **installert** og støttet (Permissions-skjermen viser status).
  * Sjekk at riktige **tilganger** er innvilget for valgt datatype.
  * Sjekk valgt **tidsrom** (Last 24 hours / Last week / Last month).

* **Opplasting feiler**

  * Sjekk `BuildConfig.FHIR_SERVER_URL`.
  * Se logg for `ObservationUploader` (“Failed to upload chunk ...”).
  * FHIR-server må støtte R4 og `Observation` POST i transaksjon.

* **Auto-sync kjører ikke**

  * Sjekk at frekvens ≠ 0 i Settings.
  * Sjekk at funksjonen “read in background” er **available** + tillatelse gitt.
  * Se WorkManager-logger og at `MainApplication`/`WorkerFactory` er registrert.

* **Duplikater lastes opp**

  * Hvis **Allow duplicates** er slått på i Settings, tillates det.
  * Ellers sjekkes `SyncedRecord` mot `metadata.id` fra HC før sending.

---

