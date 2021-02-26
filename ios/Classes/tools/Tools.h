#import <Foundation/Foundation.h>
#import "Reachability/Reachability.h"

@interface Tools : NSObject

//Log
+ (void)log:(id)props;

//返回标识信息
+ (NSString *)resultInfo:(NSString *)info;
+ (NSString *)resultFail;
+ (NSString *)resultSuccess;

// 沙盒是否有指定路径文件夹或文件
+ (BOOL)isDirectoryExist:(NSString *)path;

// 是否是文件夹
+ (BOOL)isDirectory:(NSString *)path;

//是否是图片
+ (BOOL)isImageFile:(NSString *)path;

//是否是模拟器
+ (BOOL*)isEmulator;

//获取网络状态
+ (NSString*)getNetworkStatus:(Reachability*)reachability;

@end
