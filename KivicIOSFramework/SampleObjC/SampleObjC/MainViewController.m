//
//  MainViewController.m
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 20..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import "MainViewController.h"
#import "AppDelegate.h"

@interface MainViewController ()
@property (nonatomic, retain) AppDelegate* appDelegate;
@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    _appDelegate = (AppDelegate*)[UIApplication sharedApplication].delegate;
    
    // Do any additional setup after loading the view.
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
        [alertController dismissViewControllerAnimated:YES completion:nil];
    });
}

-(void) hudDisconnected:(NSNotification*)notification {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:@"Lost HUD connection." preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:nil];
    });
}

@end
