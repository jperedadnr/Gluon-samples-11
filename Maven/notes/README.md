Notes
=======

A JavaFX application that uses Gluon Mobile Glisten (a mobile application framework with mobile-friendly UI controls) and Gluon Mobile Connect (for cloud or local storage of notes and user settings). 
For cloud storage, you'll need a valid subscription to Gluon CloudLink. You can get it [here](http://gluonhq.com/products/cloudlink/buy/), and there is a 30-day free trial.

Instructions
------------
- Set JDK 11

To execute the sample on HotSpot, do as follows:

* Desktop (Windows, Mac OS X, Linux)
  - Just run it from your IDE or from command line: `mvn -Photspot javafx:run`

To execute the sample as native image, do as follows:

* Desktop (Mac OS X, Linux)
  - Just run it from your IDE or from command line: 
     - `mvn client:compile`
     - `mvn client:link` 
     - `mvn client:run`

* iOS (iOS simulator)
   - `mvn client:compile@iossim`
   - `mvn client:link@iossim`
   - `mvn client:run@iossim`

* iOS (iOS device)
   - `mvn client:compile@ios`
   - `mvn client:link@ios`
   - Connect your iOS device with valid signing identity and provisioning profile (see [deployment documentation](https://docs.gluonhq.com/client/#_ios_deployment))
   - Run `mvn client:run@ios`

Documentation
-------------

See [Gluon Client documentation](https://docs.gluonhq.com/client)

Read how to create this sample step by step [here](http://docs.gluonhq.com/samples/notes/)
