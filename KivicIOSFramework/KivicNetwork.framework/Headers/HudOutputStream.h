//
//  HudOutputStream.h
//  HudControl
//
//  Created by scpark on 2016. 8. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HudOutputStream : NSObject
-(NSInteger) size;
-(nullable NSData*) toNSData;
-(void) toByteArray:(nonnull void *)buffer length:(NSUInteger)length;
-(void) write:(nonnull const void *)buffer length:(NSUInteger)length;
-(void) directWrite:(uint8_t) data;
-(void) writeByte:(uint8_t) data;
-(void) writeShort:(int16_t) data;
-(void) writeInt:(int32_t) data;
-(void) writeLong:(int64_t) data;
-(void) writeFloat:(float) data;
-(void) writeDouble:(double) data;
-(void) writeBoolean:(BOOL) data;
-(void) writeUTF:(nonnull NSString*) data;
@end
