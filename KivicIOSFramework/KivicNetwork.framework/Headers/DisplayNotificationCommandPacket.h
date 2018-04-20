//
//  DisplayNotificationCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 9. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface DisplayNotificationCommandPacket : CommandPacket
@property (nonatomic, assign) BOOL enable;
@end
