//
//  DisplayNotificationSettingCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 9. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

extern NSString* const DEFAULT_CALL_PACKAGE_NAME;
extern NSString* const DEFAULT_SMS_PACKAGE_NAME;
extern NSString* const DEFAULT_MUSIC_PACKAGE_NAME;
extern NSString* const DEFAULT_EMAIL_PACKAGE_NAME;
extern NSString* const DEFAULT_OBD2_PACKAGE_NAME;
extern NSString* const DEFAULT_KAKAO_TALK_PACKAGE_NAME;
extern NSString* const DEFAULT_WHATSAPP_PACKAGE_NAME;
extern NSString* const DEFAULT_FACEBOOK_PACKAGE_NAME;
extern NSString* const DEFAULT_WECHAT_PACKAGE_NAME;
extern NSString* const DEFAULT_LINE_PACKAGE_NAME;
extern NSString* const DEFAULT_SKYPE_PACKAGE_NAME;
extern NSString* const DEFAULT_VIBER_PACKAGE_NAME;
extern NSString* const DEFAULT_TANGO_PACKAGE_NAME;
extern NSString* const DEFAULT_NIMBUZZ_PACKAGE_NAME;
extern NSString* const DEFAULT_KIK_PACKAGE_NAME;
extern NSString* const DEFAULT_TELEGRAM_PACKAGE_NAME;

typedef NS_ENUM(int32_t, IconType){
    ICON_NONE = 0,
    ICON_CALL = 1,
    ICON_SMS = 2,
    ICON_MUSIC = 3,
    ICON_EMAIL = 4,
    ICON_OBD2 = 5,
    ICON_KAKAO_TALK = 6,
    ICON_WHATSAPP = 7,
    ICON_FACEBOOK = 8,
    ICON_WECHAT = 9,
    ICON_LINE = 10,
    ICON_SKYPE = 11,
    ICON_VIBER = 12,
    ICON_TANGO = 13,
    ICON_NIMBUZZ = 14,
    ICON_KIK = 15,
    ICON_TELEGRAM = 16,
    ICON_MAX = 17
};

@interface DisplayNotificationSettingCommandPacket : CommandPacket
@property (nonatomic, assign) BOOL enable;
@property (nonatomic, assign) int32_t textColor;
@property (nonatomic, assign) IconType icon;

-(void) addPackageName:(NSString*) packageName;
-(void) removePackageName:(NSString*) packageName;
-(BOOL) containPackageName:(NSString*) packageName;
-(NSArray<NSString*> *) getPackageNameList;

+(DisplayNotificationSettingCommandPacket*) getDefaultSettingPacketWith:(IconType) iconType;
@end
