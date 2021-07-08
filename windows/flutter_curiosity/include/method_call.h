#ifndef FLUTTER_PLUGIN_DESKTOP_WINDOW_PLUGIN_WINDOW_METHOD_CALL_
#define FLUTTER_PLUGIN_DESKTOP_WINDOW_PLUGIN_WINDOW_METHOD_CALL_

#include <windows.h>
#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>
#include <flutter/standard_method_codec.h>

namespace CuriosityPluginMethodCall
{
    class MethodCall
    {
    public:
        MethodCall(const flutter::MethodCall<flutter::EncodableValue> &Cmethod_call,
                   std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> Cresult);

        void setFullscreen();
        void getFullscreen();
        void toggleFullscreen();
        void setBorders();
        void hasBorders();
        void toggleBorders();
        void stayOnTop();
        void focus();

    private:
        const flutter::MethodCall<flutter::EncodableValue> &method_call;
        std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result;
    };
}

#endif
