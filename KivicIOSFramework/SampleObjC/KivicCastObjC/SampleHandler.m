//
//  SampleHandler.m
//  KivicCastObjC
//
//  Created by kjchoi on 2018. 4. 20..
//  Copyright © 2018년 kivic. All rights reserved.
//
#import <KivicCast/KivicCast.h>

#import "SampleHandler.h"

@interface SampleHandler ()
@property (nullable, nonatomic, retain) KivicCastService* kivicCastService;
@property (nonatomic, assign) float portraitScaleForKivicCast; // from 0(original) to 1(wide screen).
@end

@implementation SampleHandler

- (void)broadcastStartedWithSetupInfo:(NSDictionary<NSString *,NSObject *> *)setupInfo {
    // User has requested to start the broadcast. Setup info from the UI extension can be supplied but optional.
    
    _portraitScaleForKivicCast = 0;
    _kivicCastService = [[KivicCastService alloc] init];
    [_kivicCastService startBroadcast];
    [_kivicCastService setPortraitScale:_portraitScaleForKivicCast];
}

- (void)broadcastPaused {
    // User has requested to pause the broadcast. Samples will stop being delivered.
}

- (void)broadcastResumed {
    // User has requested to resume the broadcast. Samples delivery will resume.
}

- (void)broadcastFinished {
    // User has requested to finish the broadcast.
    if(_kivicCastService != nil) {
        [_kivicCastService stopBroadcast];
    }
    _kivicCastService = nil;
}

- (void)processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType {
    
    switch (sampleBufferType) {
        case RPSampleBufferTypeVideo:
//            [_kivicCastService setPortraitScale:_portraitScaleForKivicCast];
            [_kivicCastService appendVideoSampleBuffer:sampleBuffer];
            break;
        case RPSampleBufferTypeAudioApp:
            // Handle audio sample buffer for app audio
            break;
        case RPSampleBufferTypeAudioMic:
            // Handle audio sample buffer for mic audio
            break;
            
        default:
            break;
    }
}

@end
