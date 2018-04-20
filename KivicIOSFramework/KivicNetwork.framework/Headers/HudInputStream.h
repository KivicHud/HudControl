//
//  HudInputStream.h
//  HudControl
//
//  Created by scpark on 2016. 8. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HudInputStream : NSObject
+(nullable id) initWithNSData:(nonnull NSData*)data;
+(nullable id) initWithBytes:(nonnull const void *)bytes length:(NSUInteger)length;
-(NSInteger) available;
-(void) read:(nonnull void *)buffer length:(NSUInteger)length;
-(uint8_t) directRead;
-(uint8_t) readByte;
-(int16_t) readShort;
-(int32_t) readInt;
-(int64_t) readLong;
-(float) readFloat;
-(double) readDouble;
-(BOOL) readBoolean;
-(nonnull NSString*) readUTF;
@end
