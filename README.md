# React Native module for Pollfish

This module contains the native Android implementation. The iOS one is under the React Native app (`ios/PollfishModule.m`)

PollfishModule.h:
```
#ifndef PollfishModule_h
#define PollfishModule_h

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
@interface PollfishModule : RCTEventEmitter <RCTBridgeModule>

@end

#endif /* PollfishModule_h */
```

PollfishModule.m:
```
#import <Foundation/Foundation.h>
#import <React/RCTUtils.h>
#import <UIKit/UIKit.h>
#import "PollfishModule.h"
#import <Pollfish/Pollfish.h>

@import AdSupport;

@implementation PollfishModule

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"onPollfishEvent"];
}

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(startOfferwall:(NSString *)appkey userid:(NSString *)userid isProd:(NSString *)isProd)
{
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pollfishClosed) name:@"PollfishClosed" object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pollfishReceived:) name:@"PollfishSurveyReceived" object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pollfishNotAvailable) name:@"PollfishSurveyNotAvailable" object:nil];
  
  [self sendEventWithName:@"onPollfishEvent" body:@"onPollfishStarted"];
  
  PollfishParams *pollfishParams =  [PollfishParams initWith:^(PollfishParams *pollfishParams) {
    if ([isProd  isEqual: @"false"]) {
      pollfishParams.releaseMode=false;
    }
    if ([isProd  isEqual: @"true"]) {
      pollfishParams.releaseMode=true;
    }
    pollfishParams.offerwallMode=true;
    pollfishParams.rewardMode=true;
    pollfishParams.requestUUID=userid;
  }];
  
  [Pollfish initWithAPIKey:appkey andParams:pollfishParams];
}

- (void)pollfishClosed
{
  [Pollfish hide];
  [self sendEventWithName:@"onPollfishEvent" body:@"onPollfishClosed"];
}

- (void)pollfishReceived:(NSNotification *)notification
{
  NSLog(@"Pollfish: Survey Received - Offerwall available");
  [Pollfish show];
}

- (void)pollfishNotAvailable
{
  NSLog(@"Pollfish: Offerwall Not Available");
  [self sendEventWithName:@"onPollfishEvent" body:@"onPollfishFailed"];
}

@end
```

React Native:
```
const { PollfishModule } = NativeModules;

const pollfishModule = new NativeEventEmitter(PollfishModule);
     pollfishModule.addListener(trackedEvent, eventName => {
          this.pollfishCallback(eventName);
     });
     
pollfishCallback = value => {
    switch (value) {
      case 'onPollfishStarted':
        this.setState({ isPollfishLoading: true });
        break;
      case 'onPollfishClosed':
        this.setState({ isPollfishLoading: false });
        break;
      case 'onPollfishFailed':
        this.setState({ isPollfishLoading: false });
        this.showDialogFailedPollfish();
        break;
      default:
        break;
    }
  };
  
  
PollfishModule.startOfferwall(pollfishAppKey, uuid, String(isProd));
```

