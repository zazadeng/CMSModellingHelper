
import 'dart:html';
import 'dart:json';
//import 'package:js/js.dart' as js;

final textarea_submit_id = "#textarea";
const textarea_sql_id = "#sql";
const textarea_input_id = "#input";
const textarea_output_id = "#output";
const button_send_id = "#button";
const replace_text_id = "#textToReplace";
final div_replacement_id = "#replacement";
const replace_button = "#replaceButton";
const wordsForUser = "Please input a select sql, and click the button above...";
final inputTextHints = "Please drag and drop a text here for replacement";
const whatToReplace = "What to replace";
const yellowBackgroundSpan="<span style=\"background-color:yellow\">";
const redBackgroundSpan="<span style=\"background-color:red;color:black\">";

num rotatePos = 0;
String _textToBeReplaced = "";




void main() {
 
  query(textarea_submit_id).on.click.add(hideText);
  query(textarea_submit_id).on.blur.add(showText);
  query(button_send_id).on.click.add(submitSql);
  query(textarea_sql_id).on.mouseUp.add(highlightText);
  query(replace_text_id).on.click.add(removeAllInputValue);
  query(replace_text_id).on.keyUp.add((Event e){
    InputElement input = query(replace_text_id);
    input.value = input.value.replaceAll(new RegExp("$whatToReplace"r"(.+)"),"");
    input.style.color = "black";
  });
  query(replace_button).on.click.add(replaceText);
  
}


void replaceText(Event e){
  InputElement input = query(replace_text_id);
  String text = input.value;
 
  TableCellElement sql = query(textarea_sql_id);
  TableCellElement inputStruct = query(textarea_input_id);
  TableCellElement outStruct = query(textarea_output_id);
  
  if(_textToBeReplaced.isEmpty){
    //do nothing
  }else{
    sql.innerHTML = sql.text.replaceAll(_textToBeReplaced, "$redBackgroundSpan""$text""</span>");
    inputStruct.innerHTML = inputStruct.text.replaceAll(_textToBeReplaced, "$redBackgroundSpan""$text""</span>");
    outStruct.innerHTML = outStruct.text.replaceAll(_textToBeReplaced, "$redBackgroundSpan""$text""</span>");
  }
  query(div_replacement_id).style.display = "none";
}

void removeAllInputValue(Event e){
  InputElement input = query(replace_text_id);
  input.value = "";
  input.style.color = "black";
}

void highlightText(Event e){
  //DART way:it can't make IE9 work
  String text = window.getSelection().toString(); 
   
    
    TableCellElement sql = query(textarea_sql_id);
    TableCellElement inputStruct = query(textarea_input_id);
    TableCellElement outStruct = query(textarea_output_id);
    if(text == ""){
      sql.innerHTML = sql.text.replaceAll(r"<.*>", "");
      inputStruct.innerHTML = inputStruct.text.replaceAll(r"<.*>", "");
      outStruct.innerHTML = outStruct.text.replaceAll(r"<.*>", "");
      query(div_replacement_id).style.display = "none";
      _textToBeReplaced = "";
      return;
    }
    
    _textToBeReplaced = text;
    
    sql.innerHTML = sql.text.replaceAll(text, "$yellowBackgroundSpan""$text""</span>");
    inputStruct.innerHTML = inputStruct.text.replaceAll(text, "$yellowBackgroundSpan""$text""</span>");
    outStruct.innerHTML = outStruct.text.replaceAll(text, "$yellowBackgroundSpan""$text""</span>");
    InputElement inputElem = query(replace_text_id);
    inputElem.value = "$whatToReplace"" \"""$text""\"";
    
    query(div_replacement_id).style.display = "inline";
    
    inputElem.style.color = "grey";
    inputElem.setSelectionRange(0, 0, "backward");
    inputElem.focus();
}

void setVisible(Event e){
  DivElement divElem = query(div_replacement_id);
  divElem.style.display = "inline";
}

void selectAllInput(Event e){
  //TODO highlight all 
//  ParagraphElement elem = query(textarea_input_id);
//  TextAreaElement element = new TextAreaElement();
//  element.value = elem.text;
//  element.focus();
//  element.select();
//  elem.insertAdjacentElement("afterend", element);
// elem.remove();
}
void selectAllOutput(Event e){
  TextAreaElement elem = query(textarea_output_id);
  elem.select();
}




void hideText(Event event){
  TextAreaElement elem = query(textarea_submit_id);
  if(elem.value == wordsForUser)
    elem.value = "";
  elem.style.color = "black";
  elem.spellcheck = false;
}

void showText(Event event){
  TextAreaElement elem = query(textarea_submit_id);
  if(elem.value.trim().length == 0)
    elem.value = wordsForUser;
}

void submitSql(Event event) {
  rotatePos += 360;

  var textElement = query(button_send_id);
  //annimation for rotation of the button
  textElement.style.transition = "1s";
  textElement.style.transform = "rotate(${rotatePos}deg)";
  
  //data to server
  TextAreaElement textAreaElement = query(textarea_submit_id);//adding this to avoid warning... why can't cast
  if(textAreaElement.value == wordsForUser){
    window.alert(wordsForUser);
  }else{
    String sqlData = "sql=${textAreaElement.value}";
    //String sqlData = "sql=jjjjj";
    //window.alert("about to do this input:${sqlData}");
    sendSqlData(sqlData, onSuccess); // send the data to
  }                                   // the server
}



sendSqlData(String data, onSuccess(HttpRequest req)) {
  HttpRequest req = new HttpRequest(); // create a new XHR

  var url = "http://192.168.1.100:8080/modelmaker/ModellingResult";
  req.open("POST", url); // POST, async
  req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  req.on.readyStateChange.add((Event e) {
    if (req.readyState == HttpRequest.DONE &&
        (req.status == 200 || req.status == 0)) {
      onSuccess(req); // called when the POST successfully completes
    }
  });

  req.send(data); // kick off the request to the server
}

/**
 * parse the raw json response text(map of data) from the server returned [req]. 
 */
onSuccess(HttpRequest req) {
  
  Map jsonMap = JSON.parse(req.responseText);
  TableCellElement sql = query(textarea_sql_id);
  TableCellElement inputStruct = query(textarea_input_id);
  TableCellElement outStruct = query(textarea_output_id);
   
  sql.text = jsonMap["sql"];
  final RegExp regExp = new RegExp(r"<br />(\s*)");
  inputStruct.text = jsonMap["input"].replaceAll(regExp,"\n");
  outStruct.text = jsonMap["output"].replaceAll(regExp,"\n");
   
}

