//
//  NotiTimeoutCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface NotiTimeoutCommandPacket : CommandPacket
@property (nonatomic, assign) int32_t timeout;
@end
