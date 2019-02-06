//
//  CocoonCordovaViewController.h
//  HelloCordova
//
//  Created by Imanol Fernandez Gorostizag on 3/3/15.
//
//

#import <Foundation/Foundation.h>
#import <Cordova/CDVViewController.h>
#import <Cordova/CDVCommandDelegateImpl.h>
#import <Cordova/CDVCommandQueue.h>
#import <WebKit/WebKit.h>

#import "CocoonCordovaProtocol.h"

@interface CocoonCordovaViewController : CDVViewController<CocoonCordovaProtocol, WKNavigationDelegate, UIWebViewDelegate>

- (id)initWithURL: (NSURL*) url settings:(NSDictionary*)settings;
- (void)loadURL:(NSURL *)url;


+ (UIViewController*) createWithURL: (NSURL*) url settings:(NSDictionary*)settings;

@property (nonatomic, strong) NSDictionary * cocoonSettings;
@property (nonatomic, strong) NSURL * cocoonURL;
@property (nonatomic, weak) id<CocoonCordovaDelegate> cocoonDelegate;
@property (nonatomic, assign) BOOL isLocalURL;

@end
