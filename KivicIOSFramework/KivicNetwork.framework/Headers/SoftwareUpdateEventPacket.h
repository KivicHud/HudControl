//
//  SoftwareUpdateEventPacket.h
//  HudControl
//
//  Created by scpark on 2016. 11. 2..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "EventPacket.h"

@interface SoftwareUpdateEventPacket : EventPacket
@property (nonatomic, copy) NSString* serverIp;
@end
