BleDevices
=======

A JavaFX Application that shows how to use Bluetooth Low Energy service on your mobile project, both on Android and iOS.

It uses the BLE service in [Gluon Attach](https://github.com/gluonhq/attach) to access the native implementation in the mobile device.

Instructions
------------
- Set JDK 11

To execute the sample on HotSpot, do as follows:

* Desktop (Windows, Mac OS X, Linux)
  - Just run it from your IDE or from command line: `mvn javafx:run`

To execute the sample as native image, do as follows:

* Desktop (Mac OS X, Linux)
  - Just run it from your IDE or from command line: 
     - `mvn client:build` 
     - `mvn client:run`

* iOS (iOS device)
   - `mvn -Pios client:build`
   - Connect your iOS device with valid signing identity and provisioning profile (see [deployment documentation](https://docs.gluonhq.com/client/#_ios_deployment))
   - Run `mvn -Pios client:run`

Documentation
-------------

See [Gluon Client documentation](https://docs.gluonhq.com/client)