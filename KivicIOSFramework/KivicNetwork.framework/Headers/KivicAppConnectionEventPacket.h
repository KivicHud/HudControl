//
//  KivicAppConnectionEventPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "EventPacket.h"

@interface KivicAppConnectionEventPacket : EventPacket
@property (nonatomic, assign) BOOL isKivicAppConnected;
@end
