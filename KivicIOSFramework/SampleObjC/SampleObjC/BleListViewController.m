//
//  BleListViewController.m
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//
#import <CoreBluetooth/CoreBluetooth.h>
#import "AppDelegate.h"
#import "BleListViewController.h"

@interface BleListViewController () <BLEDiscoverDelegate>
@property (nonatomic, retain) AppDelegate* appDelegate;
@property (nonatomic, retain) NSMutableArray* bleList;
@property (nonatomic, assign) int scanTimeout;
@end

@implementation BleListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
 
    _appDelegate = (AppDelegate*)[UIApplication sharedApplication].delegate;
    _bleList = [NSMutableArray array];
    _scanTimeout = 10;
    
    _appDelegate.networkManager.discoverDelegate = self;
    [_appDelegate.networkManager ensureBluetoothOn];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillDisappear:(BOOL)animated {
    _appDelegate.networkManager.discoverDelegate = nil;
    [_appDelegate.networkManager stopScan];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if([segue.identifier isEqualToString:@"selectedBleAddress"]) {
        UITableViewCell *selectedCell = sender;
        NSIndexPath* selectedIndex = [self.tableView indexPathForCell:selectedCell];
        CBPeripheral* peripheral = [_bleList objectAtIndex:selectedIndex.row];
        
        _selectedBleAddress = peripheral.identifier.UUIDString;
        selectedCell.accessoryType = UITableViewCellAccessoryCheckmark;
    }
}

-(NSInteger) indexOfPeripheral:(CBPeripheral*) peripheral {
    for(NSInteger idx = 0 ; idx < _bleList.count ; ++idx) {
        CBPeripheral* p = [_bleList objectAtIndex:idx];
        if([p.identifier.UUIDString isEqualToString:peripheral.identifier.UUIDString]) {
            return idx;
        }
    }
    
    return -1;
}

-(void)bluetoothIsReady {
    NSArray<CBPeripheral*>* peripherals = [_appDelegate.networkManager getBondedDevices];
    if(peripherals != nil && peripherals.count > 0) {
        for(CBPeripheral* p in peripherals) {
            NSInteger index = [self indexOfPeripheral:p];
            if(index < 0) {
                [_bleList addObject:p];
            }
            else {
                [_bleList replaceObjectAtIndex:index withObject:p];
            }
        }
        
        [self.tableView beginUpdates];
        NSIndexPath* newIndexPath = [NSIndexPath indexPathForRow:(_bleList.count - peripherals.count) inSection:0];
        [self.tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
        [self.tableView endUpdates];
    }
    
    [_appDelegate.networkManager startScan:_scanTimeout];
}

-(void)didDiscoverPeripheral:(CBPeripheral *)peripheral {
    NSInteger index = [self indexOfPeripheral:peripheral];
    if(index < 0) {
        [_bleList addObject:peripheral];
        index = _bleList.count - 1;
        NSIndexPath* newIndexPath = [NSIndexPath indexPathForRow:index inSection:0];
        [self.tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
    else {
        [_bleList replaceObjectAtIndex:index withObject:peripheral];
        NSIndexPath* newIndexPath = [NSIndexPath indexPathForRow:index inSection:0];
        [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _bleList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"bleAddressCell" forIndexPath:indexPath];
    
    if(cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"bleAddressCell"];
    }
    
    CBPeripheral* peripheral = [_bleList objectAtIndex:indexPath.row];
    if(peripheral.name == nil) {
        cell.textLabel.text = peripheral.identifier.UUIDString;
    }
    else {
        cell.textLabel.text = peripheral.name;
    }
    return cell;
}
@end
