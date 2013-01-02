import 'dart:io';


/*
 * This will generate redis-SET-command file for mass inserstion of data into redis server.
 * The File name will be something like:
 *        <output_prefix>_2012-12-13 22:49:15.379.txt
 * e.g.:  JavaDtls_2012-12-13 22/49/15.379.txt
 * Each line has the FORMAT: SET <entityName>_<attribute> <JsonValue>
 * e.g.: SET WCOClaim_ClaimID "{EntityName:WCOClaim, Attribute:claimID, DomainDef:CASE_ID}"
 * 
 * NOTE here, the key is following is the exact name in the generated JAVA file,
 * so it is very easy to update/correct a particular entry in this NOSQL db...
 * 
 * Later when the server is on, we can have the following in the terminal:
 *        cat <output> | ~/redis-2.6.7/src/redis-cli 
 * e.g.:  cat JavaDtls_2012-12-13\ 22\:49\:15.379.txt | ~/redis-2.6.7/src/redis-cli
 */
void main() {
  var options = new Options();
  var args = options.arguments;
  if(args.isEmpty || (args.length !=2)){
    print("usage: Dart-SDK/bin/dart JavaDtlsFilesFinder.dart <root_path> <output_path>");
    return;
  }
  
  var root = args[0]; //'~/ccp/NextPlus/CMS/EJBServer/build/svr/gen/curam'; 
  var outputName = args[1]; //'~/JavaDtls';
  var now = new Date.now().toString().replaceAll(" ", "").replaceAll(":", "");
  
  var file = new File('$outputName''_''$now''.txt');
  file.createSync();
  var lister = new Directory(root).list(recursive: true);
  lister.onDir = (dir){
    if(dir.endsWith('struct')){
      findDtlsJava(dir, file);
    }
  };
}



findDtlsJava(String dir, File outputFile) {
  var lister = new Directory(dir).list();
  lister.onFile = (String path) {
    if (path.toLowerCase().endsWith("dtls.java")) {
      parseDtls(path, outputFile);
    }
  };
}

//has to do it in sync mode, 
//because the wantet values have dependencies among them to preserve 
//the right information. It will be overkill to have the content
//break(but we still have to lock the content for this breaking job)
//into smaller-independent contents.
parseDtls(String path, File outputFile) {
  //print(path);
  /*new File(path).readAsLines(Encoding.ISO_8859_1).handleException((exception) => print(':OH::::'
      '$path'));*/
  new File(path).readAsLines(Encoding.ISO_8859_1).then((lines){
   
    String entityName = "";
    for(String line in lines){
      Match match = new RegExp(r"public(\s+)final(\s+)class(\s+)(\w+)(D|d)tls").firstMatch(line);
      if(match != null){
        var value = match.group(0);
        entityName = value.split(new RegExp(r"\s+"))[3]
                            .replaceAll("Dtls", "")
                            .replaceAll("dtls", "");
        break;
      }
    }
    
    String domainDefinition  = "";
    var jsonStr = "";
    for(String line in lines){
      //var match = new RegExp(r"\*\s+[\w_]+\s+->").firstMatch(line);
      var match = new RegExp(r"\*\s+[A-Z0-9_]+\s+(->)?").firstMatch(line);
      if(match != null){
        var value = match.group(0);
        domainDefinition = value.replaceAll("*", "").replaceAll("->", "").trim();
      }
      match = new RegExp(r"public\s+\w+\s+\w+\s*=").firstMatch(line);
      if(match != null){
        var value = match.group(0);
        //print(match.group(0));
        var attribute = value.split(new RegExp(r"\s+"))[2];
        jsonStr = "$jsonStr"
                  "\r\n"
                  "SET"
                  " "
                  "${entityName.toUpperCase()}""_""${attribute.toUpperCase()}"
                  " "
                  "\"{"
                        "EntityName:$entityName"
                        ", Attribute:${attribute}"
                        ", DomainDef:$domainDefinition"
                     "}\"";
       
      }
    }
    if(!jsonStr.isEmpty){
      var ramAccFile= outputFile.openSync(FileMode.APPEND);
      ramAccFile.writeStringSync(jsonStr);
      ramAccFile.closeSync();
    }
  });
  
}

