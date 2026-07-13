import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

class ImagePickerModel {
  static Future<ImageSource?> showImagePicker(BuildContext context) async {
    return showModalBottomSheet<ImageSource?>(
      context: context,
      builder: (context) {
        return SafeArea(
            child: Container(
                child: Wrap(children: [
          ListTile(
            leading: Icon(FontAwesomeIcons.camera),
            title: Text('Dari Kamera'),
            onTap: () {
              Navigator.pop(context, ImageSource.camera);
            },
          ),
          ListTile(
            leading: Icon(FontAwesomeIcons.photoVideo),
            title: Text('Dari Galery Handphone'),
            onTap: () {
              Navigator.pop(context, ImageSource.gallery);
            },
          )
        ])));
      },
    );
  }

  static Future<XFile?> takePicture(
    BuildContext context,
    ImageSource? imageSource,
  ) async {
    SizeConfig().initSize(context);

    try {
      if (imageSource != null) {
        XFile? _file = await ImagePicker()
            .pickImage(source: imageSource, imageQuality: 100);

        return _file;
      } else {
        return null;
      }
    } catch (e) {
      return null;
    }
  }
}
