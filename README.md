# CopyCheck

CopyCheck is used to validate that files were properly moved from a source to destination.  The typical use cases for CopyCheck would be to verify the move of a very large number of files or to check that two folders contain the same versions of a set of files.

## Usage

```java -jar CopyCheck-0.0.1 <required> <optional>```
### Required Arguments
* ```--source=<path>``` : path is the absolute path files were copied or moved from
* ```--destination=<path>``` : path (relative or absolute) is the location files were moved to
* ```--srcFileList=<path>``` : path (relative or absolute) is the location of the srcFile list (description below)
### Optional Arguments
* ```--NO-HASH``` : No hash algorithm used.  Only checks the existence of a file at the destination.
* ```--MD5``` : uses an MD5 hash algorithm.  Expects 128 bit hexadecimal digests.
* ```--SHA-1``` : uses an SHA-1 hash algorithm.  Expects 160 bit hexadecimal digests.
* ```--SHA-256``` : uses an SHA-256 hash algorithm.  Expects 256 bit hexadecimal digests.
##### Default: --NO-HASH
### Source File List (srcFileList)
Is just a list of source file paths and hash digests.  Paths should be absolute.  Digests should be in hexadecimal (not required with --NO-HASH).   The format of the srcFileList is very flexible, each field must be delimited by two or more space characters.
#### Formatting Examples:
<table> 
    <thead>
        <tr>
            <td>
                Line Text
            </td>
            <td>
                Validity
            </td>
            <td>
                Explanation
            </td>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                <pre>/source/dir/test/1  3f786850e387550fdab836ed7e6dc881de23001b</pre>
            </td>
            <td>
                OK!           
            </td>
            <td>
            </td>
        </tr>
        <tr>
          <td>
            <pre>/source/dir/test/1 3f786850e387550fdab836ed7e6dc881de23001b</pre> 
          </td>
          <td>
            NOT OK
          </td>
          <td>
            only single space between columns
          </td>
        </tr>
        <tr>
            <td>
                <pre>3f786850e387550fdab836ed7e6dc881de23001b  /source/dir/test/1</pre>
            </td>
                OK!
            <td> 
            </td>
            <td> 
            </td>
        </tr>
        <tr>     
            <td>
                <pre>530  1-Mar-2022 18:12 /source/dir/test/1  3f786850e387550fdab836ed7e6dc881de23001b</pre>
            </td>
                OK!
            <td> 
            </td>
            <td> 
            </td>
        </tr>
        <tr>     
            <td>
                <pre>/source/dir/test/3 hasSpace  2b66fd261ee5c6cfc8de7fa466bab600bcfe4f69</pre>
            </td>
                OK!
            <td>
                parsed path is <code>/source/dir/test/3 hasSpace"</code>
            </td>
            <td> 
            </td>
        </tr>
        <tr>     
            <td>
                <pre>/source/dir/test/3  hasSpaces  2b66fd261ee5c6cfc8de7fa466bab600bcfe4f69</pre>
            </td>
            <td>
                OK!
            </td>
                parsed path is <code>/source/dir/test/3</code>
            <td> 
            </td>
        </tr>    
    </tbody>
</table> 
## Build
```gradle build```
## Demo

<div> <pre>java -jar build/libs/CopyCheck-0.0.1.jar --source=/source/dir/test --destination=src/test/resources/demo --srcFileList=src/test/resources/demo.list --SHA-1 </pre></div>

### Result
```
Valid:   /source/dir/test/1
Valid:   /source/dir/test/childFolder/2
Valid:   /source/dir/test/3 hasSpace
Valid:   /source/dir/test/5
Invalid: /source/dir/test/4 notAFile
Invalid: /source/dir/test/4 willHashFail

Summary: 4 of 6 are valid
```
### Explanation
- CopyCheck relativizes paths against ```/source/dir/test``` (source): <div>```/source/dir/test/childFolder/2 -> childFolder/2```</div>
- CopyCheck resolves the relativized path with ```src/test/resources/demo``` (destination): <div>```childFolder/2 -> src/test/resources/demo/childFolder/2```</div>
- CopyCheck hashes this file, if present
- The hash code is compared to the code read in from srcFileList.
### Invalid
- ```/source/dir/test/4 notAFile``` Failed because the file didn't exist.
- ```/source/dir/test/4 willHashFail``` Failed because the hash code of the file didn't match the srcFileList.
