
Comments
========

 A JavaFX Application that uses Gluon Mobile: 

 - CloudLink Client: the client-side counterpart of the Gluon CloudLink service 
 - Glisten: Simple application framework, custom UI controls and customized existing JavaFX UI controls

Prerequisites
-------------

You need a valid subscription to Gluon CloudLink. You can get it [here](http://gluonhq.com/products/cloudlink/buy/), and 
there is a 30-day free trial. Sign up and get a valid account on Gluon CloudLink and a link to download the Gluon CloudLink 
Dashboard. 

Install and open the Dashboard, and sign in using the Gluon account credentials provided above. Go to the App Management view, and you will 
find a pair of key/secret tokens. Save the file `gluoncloudlink_config.json` under your project 
`src/main/resources/` folder. The content of the file is a JSON object with the key and secret that will grant access
to Gluon CloudLink:

```json
{
  "gluonCredentials": {
    "applicationKey": "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
    "applicationSecret": "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
  }
}
```

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

Read how to create this sample step by step [here](http://docs.gluonhq.com/samples/comments/)
