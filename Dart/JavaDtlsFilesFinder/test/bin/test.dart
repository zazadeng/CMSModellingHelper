import 'dart:io';
import 'package:/unittest/unittest.dart';

void main() {
  var fileName = '/Users/<username>/GenJavaDtls_2012-12-14211035.701.txt';//FIXME: pls change me
  var list = new File(fileName).readAsLinesSync();
  int isNOTBeginningWithSET = -1;
  int isNOTEndWithQuote = -1;
  int isNOTMatchingKeyFormat = -1;
  int isNOTMatchingValueFormat = -1;
  int counter = 0;
  for(String line in list){
    counter++;
    if(line.isEmpty){
      continue;
    }
    if(!new RegExp(r"^SET").hasMatch(line)){
      isNOTBeginningWithSET = counter;
      break;
    }
    if(!new RegExp(r'"$').hasMatch(line)){
      isNOTEndWithQuote = counter;
      break;
    }
    if(!new RegExp(r'^SET[ ]{1}\w+_\w+[ ]{1}".+').hasMatch(line)){
      isNOTMatchingKeyFormat = counter;
      break;
    }
    if(!new RegExp(r'^SET[ ]{1}\w+_\w+[ ]{1}"{(\w+:\w+,[ ]{1}){2}\w+:[A-Z_0-9]+}.+').hasMatch(line)){
      isNOTMatchingValueFormat = counter;
      break;
    }
  }
  
  test('Check if there is a line which is NOT starting with SET',
       () => expect(isNOTBeginningWithSET<0, isTrue, reason:'Line $isNOTBeginningWithSET is the defect line.'));
  
  test('check if line did NOT end with quote(")',
      () => expect(isNOTEndWithQuote<0, isTrue, reason:'Line $isNOTEndWithQuote is the defect line.'));
  
  test('Check if there is a line which key is not formated correctly',
      () => expect(isNOTMatchingKeyFormat<0, isTrue, reason:'Line $isNOTMatchingKeyFormat is the defect line.'));

  test('Check if there is a line which value is not formated correctly',
      () => expect(isNOTMatchingValueFormat<0, isTrue, reason:'Line is $isNOTMatchingValueFormat is the defect line.'));
  
}
