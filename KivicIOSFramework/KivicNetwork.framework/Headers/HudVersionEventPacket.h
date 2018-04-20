//
//  HudVersionEventPacket.h
//  HudControl
//
//  Created by scpark on 2016. 11. 10..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "EventPacket.h"

@interface HudVersionEventPacket : EventPacket
@property (nonatomic, copy) NSString* modelName;
@property (nonatomic, copy) NSString* systemVersion;
@property (nonatomic, copy) NSString* bleVersion;
@end
