//
//  KivicCastService.h
//  KivicCast
//
//  KivicCastService is a high level framework that can be used to broadcast iPhone screen to Kivic HUD device.
//
//  Created by scpark on 2018. 4. 11..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <ReplayKit/ReplayKit.h>

@interface KivicCastService : NSObject
/* @abstract The scale ratio to fit screen in the portrait mode. Seting scale to 1.0f produces stretch video to fit screen. Must be from 0.0f to 1.0f.*/
@property (nonatomic, assign) CGFloat portraitScale;

/* @abstract Starts screen broadcast */
- (void)startBroadcast;
/* @abstract Stops screen broadcast */
- (void)stopBroadcast;
/* @abstract Enqueues video sample buffer */
- (void)appendVideoSampleBuffer:(nonnull CMSampleBufferRef)sampleBuffer;
@end
