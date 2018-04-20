//
//  CommandController.m
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//
#import <KivicNetwork/KivicNetwork.h>

#import "CommandController.h"
#import "AppDelegate.h"

@interface CommandController ()
@property (nonatomic, retain) AppDelegate* appDelegate;
@end

@implementation CommandController

- (void)viewDidLoad {
    [super viewDidLoad];
    _appDelegate = (AppDelegate*)[UIApplication sharedApplication].delegate;
    
    if(_appDelegate.networkManager.isConnected) { // if Kivic HUD is connected
        [self sendSystemTime];
        [self sendBacklight:YES];
        [self sendSpeedColor:0xFFFFFFFF];
        [self sendSpeedUnit:KM_PER_HOUR];
        [self sendKeyStone:0];
        [self sendFullScreen:YES];
        [self sendMode:IOS_HUD_MODE];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillAppear:(BOOL)animated {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hudConnected:) name:HudConnectedNotificationKey object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hudConnectionFailed:) name:HudConnectionFailedNotificationKey object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hudDisconnected:) name:HudDisconnectedNotificationKey object:nil];
}

-(void)viewWillDisappear:(BOOL)animated {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(void) hudConnected:(NSNotification*)notification {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:@"HUD connected." preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:nil];
    });
}

-(void) hudConnectionFailed:(NSNotification*)notification {
    NSError* error = notification.userInfo[HudErrorExtraKey];
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"HUD connection fail." message:[error localizedFailureReason] preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:^{
            [self.navigationController popViewControllerAnimated:YES];
        }];
    });
}

-(void) hudDisconnected:(NSNotification*)notification {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:@"Lost HUD connection." preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:^{
            [self.navigationController popViewControllerAnimated:YES];
        }];
    });
}

-(BOOL) sendSystemTime {
    SystemTimeCommandPacket* systemTimeCommandPacket = [[SystemTimeCommandPacket alloc] init];
    
    systemTimeCommandPacket.timeZoneId = [NSTimeZone systemTimeZone].name;
    systemTimeCommandPacket.timeInMillis = (int64_t)([[NSDate date] timeIntervalSince1970]*1000);
    if([_appDelegate.networkManager sendPacket:systemTimeCommandPacket]) {
        DisplayTimeCommandPacket* displayTimeCommandPacket = [[DisplayTimeCommandPacket alloc] init];
        
        displayTimeCommandPacket.isEnable = YES;
        return [_appDelegate.networkManager sendPacket:displayTimeCommandPacket];
    }
    
    return NO;
}

-(BOOL) sendSpeedColor:(int32_t) speedColor {
    DisplaySpeedColorCommandPacket* sendPacket = [[DisplaySpeedColorCommandPacket alloc] init];
    
    sendPacket.textColor = speedColor;
    return [_appDelegate.networkManager sendPacket:sendPacket];
}

-(BOOL) sendBacklight:(BOOL) enable {
    DisplayBrightnessCommandPacket* sendPacket = [[DisplayBrightnessCommandPacket alloc] init];
    
    sendPacket.isBacklightEnabled = enable;
    return [_appDelegate.networkManager sendPacket:sendPacket];
}

-(BOOL) sendSpeedUnit:(SpeedUnitType) speedUnit {
    DisplaySpeedUnitsCommandPacket* sendPacket = [[DisplaySpeedUnitsCommandPacket alloc] init];
    
    sendPacket.speedUnitType = speedUnit;
    return [_appDelegate.networkManager sendPacket:sendPacket];
}

-(BOOL) sendMode:(KivicModeType) mode {
    KivicModeCommandPacket* sendPacket = [[KivicModeCommandPacket alloc] init];
    
    sendPacket.mode = mode;
    return [_appDelegate.networkManager sendPacket:sendPacket];
}

-(BOOL) sendKeyStone:(float) keyStone {
    KeyStoneCommandPacket* sendPacket = [[KeyStoneCommandPacket alloc] init];
    
    sendPacket.keyStone = keyStone;
    return [_appDelegate.networkManager sendPacket:sendPacket];
}

-(BOOL) sendFullScreen:(BOOL) isFullScreen {
    FullScreenCommandPacket* sendPacket = [[FullScreenCommandPacket alloc] init];
    
    sendPacket.isFullScreen = isFullScreen;
    return [_appDelegate.networkManager sendPacket:sendPacket];
}

- (IBAction)onBacklightClicked:(UISwitch *)sender {
    [self sendBacklight:sender.isOn];
}

- (IBAction)onSpeedColorClicked:(UIButton *)sender {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"Select speed color" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction* whiteColorAction = [UIAlertAction actionWithTitle:@"White" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
       [sender setTitle:@"White >" forState:UIControlStateNormal];
        [self sendSpeedColor:0xFFFFFFFF];
    }];
    UIAlertAction* cyanColorAction = [UIAlertAction actionWithTitle:@"Cyan" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"Cyan >" forState:UIControlStateNormal];
        [self sendSpeedColor:0xFF00FFFF];
    }];
    UIAlertAction* yellowColorAction = [UIAlertAction actionWithTitle:@"Yellow" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"Yellow >" forState:UIControlStateNormal];
        [self sendSpeedColor:0xFFFFFF00];
    }];
    UIAlertAction* greenColorAction = [UIAlertAction actionWithTitle:@"Green" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"Green >" forState:UIControlStateNormal];
        [self sendSpeedColor:0xFF00FF00];
    }];
    UIAlertAction* cancelAction = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:nil];
    
    [alertController addAction:whiteColorAction];
    [alertController addAction:cyanColorAction];
    [alertController addAction:yellowColorAction];
    [alertController addAction:greenColorAction];
    [alertController addAction:cancelAction];
    
    [self presentViewController:alertController animated:YES completion:nil];
}

- (IBAction)onSpeedUnitClicked:(UIButton *)sender {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"Select speed unit" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction* kmhAction = [UIAlertAction actionWithTitle:@"KMH" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"KMH >" forState:UIControlStateNormal];
        [self sendSpeedUnit:KM_PER_HOUR];
    }];
    UIAlertAction* mphAction = [UIAlertAction actionWithTitle:@"MPH" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"MPH >" forState:UIControlStateNormal];
        [self sendSpeedUnit:MILES_PER_HOUR];
    }];
    UIAlertAction* cancelAction = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:nil];
    
    [alertController addAction:kmhAction];
    [alertController addAction:mphAction];
    [alertController addAction:cancelAction];
    
    [self presentViewController:alertController animated:YES completion:nil];
}

- (IBAction)onKeyStoneChanged:(UISlider *)sender {
    [self sendKeyStone:sender.value];
}

- (IBAction)onModeClicked:(UIButton *)sender {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"Select mode" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction* hudAction = [UIAlertAction actionWithTitle:@"HUD" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"HUD >" forState:UIControlStateNormal];
        [self sendMode:IOS_HUD_MODE];
    }];
    UIAlertAction* airplayAction = [UIAlertAction actionWithTitle:@"AirPlay" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"AirPlay >" forState:UIControlStateNormal];
        if([self sendMode:IOS_MODE]) {
            UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"How to airplay mirroing" message:@"Connect 'Kivic HUD' Wi-Fi from the iPhone's settings. Please note that the Wi-Fi password is 87654321. Then choos AirPlay Mirroring in the control center." preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction* cancelAction = [UIAlertAction actionWithTitle:@"Confirm" style:UIAlertActionStyleCancel handler:nil];
            [alertController addAction:cancelAction];
            [self presentViewController:alertController animated:YES completion:nil];
        }
    }];
    UIAlertAction* kiviccastAction = [UIAlertAction actionWithTitle:@"KivicCast" style:UIAlertActionStyleDefault handler:^(UIAlertAction* action) {
        [sender setTitle:@"KivicCast >" forState:UIControlStateNormal];
        if([self sendMode:IOS_KIVICCAST_MODE]) {
            UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"How to KivicCast" message:@"Connect 'Kivic HUD' Wi-Fi from the iPhone's settings. Please note that the Wi-Fi password is 87654321. Then long press the screen record button in ther control center and choose 'KivicCastObjC'." preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction* cancelAction = [UIAlertAction actionWithTitle:@"Confirm" style:UIAlertActionStyleCancel handler:nil];
            [alertController addAction:cancelAction];
            [self presentViewController:alertController animated:YES completion:nil];
        }
    }];
    UIAlertAction* cancelAction = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:nil];
    
    [alertController addAction:hudAction];
    [alertController addAction:airplayAction];
    [alertController addAction:kiviccastAction];
    [alertController addAction:cancelAction];
    
    [self presentViewController:alertController animated:YES completion:nil];
}

- (IBAction)onFullScreenClicked:(UISwitch *)sender {
    [self sendFullScreen:sender.isOn];
}
@end
