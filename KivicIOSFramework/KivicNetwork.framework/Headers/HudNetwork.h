//
//  HudNetwork.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "HudPacket.h"
#import "HudPacketFilter.h"

@protocol IHudNetworkImpl
-(BOOL) sendPacketImpl:(nonnull NSData*) data;
@end

@protocol PacketHandleDelegate <NSObject>
-(void) onPacketReceived:(nonnull id) receivePacket;
@end

@interface HudNetwork : NSObject <IHudNetworkImpl>
-(void) registerPacketHandleDelegate:(nonnull id<PacketHandleDelegate>) delegate;
-(void) registerPacketHandleDelegate:(nonnull id<PacketHandleDelegate>) delegate filter:(nonnull HudPacketFilter*)filter;
-(void) unregisterPacketHandleDelegate:(nonnull id<PacketHandleDelegate>) delegate;
-(void) setDumpPacket:(BOOL) dumpSendPacket dumpReceivePacket:(BOOL)dumpReceivePacket;
-(BOOL) sendPacket:(nonnull HudPacket*) hudPacket;
-(void) handleRawPacket:(nonnull NSData*) data;
@end
