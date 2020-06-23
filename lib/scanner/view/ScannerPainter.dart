import 'package:flutter/material.dart';import 'package:flutter_curiosity/tools/InternalTools.dart';class ScannerPainter extends CustomPainter {  final double value;  final Color borderColor;  final Color scannerColor;  Size size;  ScannerPainter({this.value, this.borderColor, this.size, this.scannerColor});  Paint paintValue;  @override  void paint(Canvas canvas, Size s) {    if (size == null) size = Size(s.width * 0.8, s.height * 0.4);    if (paintValue == null) {      initPaint();    }    double width = InternalTools        .getSize()        .width;    double height = InternalTools        .getSize()        .height;    double left = (width - size.width) / 2;    double top = (height - size.height) / 2;    double boxWidth = size.width;    double boxHeight = size.height;    double bottom = top + boxHeight;    double right = left + boxWidth;    paintValue.color = borderColor;    final rect = Rect.fromLTWH(left, top, boxWidth, boxHeight);    canvas.drawRect(rect, paintValue);    paintValue.strokeWidth = 3;    Path path1 = Path()      ..moveTo(left, top + 10)      ..lineTo(left, top)..lineTo(left + 10, top);    canvas.drawPath(path1, paintValue);    Path path2 = Path()      ..moveTo(left, bottom - 10)      ..lineTo(left, bottom)..lineTo(left + 10, bottom);    canvas.drawPath(path2, paintValue);    Path path3 = Path()      ..moveTo(right, bottom - 10)      ..lineTo(right, bottom)..lineTo(right - 10, bottom);    canvas.drawPath(path3, paintValue);    Path path4 = Path()      ..moveTo(right, top + 10)      ..lineTo(right, top)..lineTo(right - 10, top);    canvas.drawPath(path4, paintValue);    paintValue.color = scannerColor;    final scanRect = Rect.fromLTWH(        left + 10, top + 10 + (value * (boxHeight - 20)), boxWidth - 20, 3);    paintValue.shader = LinearGradient(colors: <Color>[      Colors.white54,      Colors.white,      Colors.white54,    ], stops: [      0.0,      0.5,      1,    ]).createShader(scanRect);    canvas.drawRect(scanRect, paintValue);  }  @override  bool shouldRepaint(CustomPainter oldDelegate) {    return true;  }  void initPaint() {    paintValue = Paint()      ..style = PaintingStyle.stroke      ..strokeWidth = 0.5      ..isAntiAlias = true      ..strokeCap = StrokeCap.round      ..strokeJoin = StrokeJoin.bevel;  }}