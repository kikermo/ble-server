[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# ble-server
BLE server-side library for the BLUEZ stack built in Kotlin. Initial implementation based on  [ble-java](https://github.com/tongo/ble-java).

This project is currently in a very early stage. I built it as a proof of concept, although I have plans to extend its compatibility and improve the API and quality. It has been tested on Raspberry Pi 3 and 5, but there shouldn't be a problem running it on any other Linux system with the BLUEZ stack.

### Get Started

To use BLE-Server, check out the project from GitHub and follow this steps:

1. Publish libraries to maven local
```
./gradlew publishToMavenLocal
```

2. Add dependencies to your project
```gradle
implementation("org.kikermo.bleserver:core:0.0.1-SNAPSHOT")
implementation("org.kikermo.bleserver:bluez:0.0.1-SNAPSHOT")
```

3. Define characteristic
```kotlin
    val characteristic = BLECharacteristic(
        uuid = UUID.fromString(CHARACTERISTIC_UUID),
        readAccess = BLECharacteristic.AccessType.Read,
        notifyAccess = BLECharacteristic.AccessType.Notify,
        name = "mycharacteristic",
    )
```

4. Create service
```kotlin
val service = BLEService(
        uuid = UUID.fromString(SERVICE_UUID),
        name = SERVICE_NAME,
        characteristics = listOf(characteristics)
    )
```
5. Define and start server
```kotlin
  val server = BLEServer(
        services = listOf(service),
        serverName = SERVER_NAME,
        bleServerConnector = BluezBLEServerConnector()
    )

    server.start()
```

### Contribution

At this stage, contributions can be made in any of the following ways:

- Feedback. You can send me your feedback to `kikerno@gmail.com`
- Create an issue on the project with anything you think is not working properly or a feature you would like to see. Issues are a good way to help a project grow and improve.
- Create a Pull Request. If something can easily improved, or cleaned up, feel free to do so. For features, it would be better to discuss an issue before creating a PR.
- Giving a star to the project. That would give visibility and attract other developers to contribute.
- Share on Slack channels or other online forums.

### Roadmap

As mentioned before, the project is in an early stage, but these are the features I would like to implement in the following months.

- Local maven publishing. August 2024.
- Quality checks Q3 2024
- DSL creation. Q3 2024
- Beta version on Manven Centra. Q4 2024
- KMP support Q1,Q2 2025


