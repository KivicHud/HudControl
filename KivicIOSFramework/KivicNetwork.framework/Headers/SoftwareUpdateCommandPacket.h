//
//  SoftwareUpdateCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 11. 2..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface SoftwareUpdateCommandPacket : CommandPacket
@property (nonatomic, assign) int64_t size;
@end
