//
//  HudPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HudInputStream.h"
#import "HudOutputStream.h"

@protocol IHudPacket
-(uint8_t) getCommand;
-(uint8_t) getParam1;
-(uint8_t) getParam2;
-(void) appendPayload:(nonnull HudOutputStream*) outputStream;
-(BOOL) parsePayload:(nonnull HudInputStream*) inputStream;
@end

@interface HudPacket : NSObject <IHudPacket>
+(void) hexDump:(nullable NSString*) tag buffer:(nonnull void const*)buffer length:(NSUInteger)length;
-(void) serialize:(nonnull HudOutputStream*) outputStream;
@end
