//
//  SystemTimeCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface SystemTimeCommandPacket : CommandPacket
@property (nonatomic, assign) int64_t timeInMillis;
@property (nonatomic, nonnull, copy) NSString* timeZoneId;
@end
