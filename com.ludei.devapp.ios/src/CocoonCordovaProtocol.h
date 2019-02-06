#pragma once



@protocol CocoonCordovaProtocol;
@protocol CocoonCordovaDelegate;
typedef UIViewController<CocoonCordovaProtocol> CocoonCordovaVC;


@protocol CocoonCordovaProtocol <NSObject>

+ (CocoonCordovaVC*) createWithURL: (NSURL*) url settings:(NSDictionary*)settings;
-(void) loadURL:(NSURL *) url;

@property (nonatomic, weak) id<CocoonCordovaDelegate> cocoonDelegate;
@end


@protocol CocoonCordovaDelegate <NSObject>

@required
//called when the user clicks exit button in debug view or forceToFinish extensin is called in JS
- (void)cocoonCordovaVCDidExit:(UIViewController *)viewController;

@optional
- (void)cocoonCordovaVCDidStartLoad:(CocoonCordovaVC *) viewController;
- (void)cocoonCordovaVCDidFinishLoad:(CocoonCordovaVC *) viewController;
- (void)cocoonCordovaVC:(CocoonCordovaVC *) viewController didFailLoadWithError:(NSError *) error;
- (BOOL)cocoonCordovaVCWillReload:(CocoonCordovaVC *) viewController;
@end

