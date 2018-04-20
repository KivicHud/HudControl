//
//  GpsStatusCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 12. 1..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface GpsStatusCommandPacket : CommandPacket
@property (nonatomic, assign) BOOL isUnreliable;
@end
