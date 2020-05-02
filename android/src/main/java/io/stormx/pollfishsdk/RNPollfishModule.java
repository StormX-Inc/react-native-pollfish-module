package io.stormx.pollfishsdk;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.pollfish.classes.SurveyInfo;
import com.pollfish.main.PollFish;
import com.pollfish.main.PollFish.ParamsBuilder;
import com.pollfish.interfaces.PollfishClosedListener;
import com.pollfish.interfaces.PollfishSurveyNotAvailableListener;
import com.pollfish.interfaces.PollfishReceivedSurveyListener;

import javax.annotation.Nullable;

public class RNPollfishModule extends ReactContextBaseJavaModule {

    public RNPollfishModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNPollfish";
    }

    @ReactMethod
    public void init(final String appKey, final String userId, final boolean isProd) {
        sendEvent("onPollfishStarted");

        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PollFish.initWith(getCurrentActivity(), new ParamsBuilder(appKey)
                            .pollfishClosedListener(new PollfishClosedListener() {
                                @Override
                                public void onPollfishClosed() {
                                    PollFish.hide();
                                    Log.d("PollFish", "onPollfishClosed");
                                    sendEvent("onPollfishClosed");
                                }
                            })
                            .pollfishReceivedSurveyListener(new PollfishReceivedSurveyListener() {
                                @Override
                                public void onPollfishSurveyReceived(@Nullable SurveyInfo surveyInfo) {
                                    Log.d("PollFish", "onPollfishSurveyReceived");
                                    sendEvent("onPollfishClosed");
                                }
                            })
                            .pollfishSurveyNotAvailableListener(new PollfishSurveyNotAvailableListener() {
                                @Override
                                public void onPollfishSurveyNotAvailable(){
                                    Log.d("PollFish", "onPollfishSurveyNotAvailable");
                                    sendEvent("onPollfishFailed");
                                }
                            })
                            .offerWallMode(true)
                            .rewardMode(true)
                            .releaseMode(isProd)
                            .requestUUID(userId)
                            .build());
                }
            });
        }
    }

    @ReactMethod
    public void startOfferwall() {
        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PollFish.show();
                }
            });
        }
    }

    private void sendEvent(String eventValue) {
        WritableMap event = Arguments.createMap();
        event.putString("value", eventValue);

        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onPollfishEvent", event);
    }

}
