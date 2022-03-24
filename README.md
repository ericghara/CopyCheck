# CopyCheck

CopyCheck is used to validate that a set of files represent a previously known snapshot.  The typical use cases for CopyCheck are to verify the move of a very large number of files or to check that two folders contain the same version of a set of files.

## Usage

```java -jar CopyCheck-0.0.1 <mode> <required> <optional>```

### Mode Arguments
* ```--destination=<path>```: checks that files at the destination match the srcFileList (see below).  The path (relative or absolute) is the location files were moved to since the snapshot was taken.
* ```--snapshot```: create a snapshot of files contained in the current directory (including files in subdirectories). 
##### Default: --snapshot
### Required Arguments
* ```--source=<path>```: in <em>snapshot</em> mode this is the starting path for file discovery; in <em>destination</em> mode this acts as a filter for resolution of destination file paths (<em>see explanation section</em>).  In most use cases, use the same source path as was used for snapshot creation.
* ```--srcFileList=<path>```: path (relative or absolute) is the location the srcFile list should be read from (<em>destination</em> mode) or written to (<em>snapshot</em> mode)
### Optional Arguments
* ```--NO-HASH``` : No hash algorithm used.  Only checks the existence of a file at the destination.
* ```--MD5``` : uses an MD5 hash algorithm.  Expects 128 bit hexadecimal digests.
* ```--SHA-1``` : uses an SHA-1 hash algorithm.  Expects 160 bit hexadecimal digests.
* ```--SHA-256``` : uses an SHA-256 hash algorithm.  Expects 256 bit hexadecimal digests.
##### Default: --NO-HASH
### Source File List (srcFileList)
The srcFileList is a list of source file paths and hash digests.  Paths must be absolute.  Digests should be in hexadecimal (not required with <em>NO-HASH</em>).   The format of the srcFileList is very flexible, with the only requirement being each field must be delimited by two or more space characters.  The most straightforward way to make a srcFileList is to run CopyCheck in <em>snapshot</em> mode, however other methods adhering to the formatting guide below may be used.

#### Formatting Examples:
<table> 
    <thead>
        <tr>
            <td>
                <b>Line Text</b>
            </td>
            <td>
                <b>Validity</b>
            </td>
            <td>
                <b>Explanation</b>
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
            <td> 
                OK!
            </td>
            <td> 
            </td>
        </tr>
        <tr>     
            <td>
                <pre>530  1-Mar-2022 18:12 /source/dir/test/1  3f786850e387550fdab836ed7e6dc881de23001b</pre>
            </td>
            <td> 
                OK!
            </td>
            <td> 
            </td>
        </tr>
        <tr>     
            <td>
                <pre>/source/dir/test/3 hasSpace  2b66fd261ee5c6cfc8de7fa466bab600bcfe4f69</pre>
            </td>
            <td>
                OK!
            </td>
            <td> 
                parsed path is <code>/source/dir/test/3 hasSpace</code>
            </td>
        </tr>
        <tr>     
            <td>
                <pre>/source/dir/test/3  hasSpaces  2b66fd261ee5c6cfc8de7fa466bab600bcfe4f69</pre>
            </td>
            <td>
                OK!
            </td>
            <td> 
                parsed path is <code>/source/dir/test/3</code>
            </td>
        </tr>    
    </tbody>
</table> 

## Build
```gradle build```
## Demo – Destination Mode

A demo srcFile list and set of files is located in the test folder.  The initial snapshot was taken in ```/source/dir/test```.  You can confirm that they have been properly cloned to your computer from this repo by running:  

<div> <pre>java -jar build/libs/CopyCheck-0.0.1.jar --source=/source/dir/test --destination=src/test/resources/demo --srcFileList=src/test/resources/demo.list --SHA-1 </pre></div>

### Result
```
>> Valid << 

Hash                                      Path
3f786850e387550fdab836ed7e6dc881de23001b  /mnt/code/java/CopyCheck/src/test/resources/demo/1
89e6c98d92887913cadf06b2adb97f26cde4849b  /mnt/code/java/CopyCheck/src/test/resources/demo/childFolder/2
2b66fd261ee5c6cfc8de7fa466bab600bcfe4f69  /mnt/code/java/CopyCheck/src/test/resources/demo/3 hasSpace
094e3afb2fe8dfe82f63731cdcd3b999f4856cff  /mnt/code/java/CopyCheck/src/test/resources/demo/5

>> Invalid << 

Expected                                  Found                                     Path
2b66fd261ee5c6cfd8de7fa466bab600bcfe4f69  Hash Failure                              /mnt/code/java/CopyCheck/src/test/resources/demo/4 notAFile
2b66fd261ee5c6cfd8de7fa466bab600bcfe4f69  da39a3ee5e6b4b0d3255bfef95601890afd80709  /mnt/code/java/CopyCheck/src/test/resources/demo/4 willHashFail

Summary: 4 of 6 are valid
```
### Explanation
- CopyCheck relativizes paths against ```/source/dir/test``` (source): <div>```/source/dir/test/childFolder/2 -> childFolder/2```</div>
- CopyCheck resolves the relativized path with ```src/test/resources/demo``` (destination): <div>```childFolder/2``` -> ```src/test/resources/demo/childFolder/2```</div>
- CopyCheck hashes this file, if present
- The hash code is compared to the code read in from srcFileList.
### Invalid
- ```/source/dir/test/4 notAFile``` Failed because the file didn't exist.
- ```/source/dir/test/4 willHashFail``` Failed because the hash code of the file didn't match the srcFileList.

## Demo – Snapshot Mode

### Action
Create a snapshot of ```src/test/resources/demo``` (note source must be an absolute path).  
<div><pre>
java -jar build/libs/CopyCheck-0.0.1.jar --source=$PWD/src/test/resources/demo --srcFileList=test.list --SHA-256 --snapshot
cat test.list
</pre></div>

### Result
<pre>
/mnt/code/java/CopyCheck/src/test/resources/demo/1  87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7
/mnt/code/java/CopyCheck/src/test/resources/demo/4 willHashFail  e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
/mnt/code/java/CopyCheck/src/test/resources/demo/childFolder/2  0263829989b6fd954f72baaf2fc64bc2e2f01d692d4de72986ea808f6e99813f
/mnt/code/java/CopyCheck/src/test/resources/demo/5  a2bbdb2de53523b8099b37013f251546f3d65dbe7a0774fa41af0a4176992fd4
/mnt/code/java/CopyCheck/src/test/resources/demo/3 hasSpace  a3a5e715f0cc574a73c3f9bebb6bc24f32ffd5b67b387244c2c909da779a1478
</pre>

### Action
Let's confirm that we can successfully copy the ```demo``` folder.
<div><pre>cp -r src/test/resources/demo src/test/resources/newDemo
java -jar build/libs/CopyCheck-0.0.1.jar --source=$PWD/src/test/resources/demo --srcFileList=test.list --SHA-256 --destination=src/test/resources/newDemo
</pre></div>

### Result
<pre>
>> Valid << 

Hash                                                              Path
87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7  /mnt/code/java/CopyCheck/src/test/resources/newDemo/1
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855  /mnt/code/java/CopyCheck/src/test/resources/newDemo/4 willHashFail
0263829989b6fd954f72baaf2fc64bc2e2f01d692d4de72986ea808f6e99813f  /mnt/code/java/CopyCheck/src/test/resources/newDemo/childFolder/2
a2bbdb2de53523b8099b37013f251546f3d65dbe7a0774fa41af0a4176992fd4  /mnt/code/java/CopyCheck/src/test/resources/newDemo/5
a3a5e715f0cc574a73c3f9bebb6bc24f32ffd5b67b387244c2c909da779a1478  /mnt/code/java/CopyCheck/src/test/resources/newDemo/3 hasSpace

Summary: 5 of 5 are valid</pre>

### Explanation
CopyCheck confirmed the copy from ```demo``` to ```demo2``` was successful.  In this example the <em>source</em> path is the same as was used for snapshot creation.

### Action
Let's confirm that the ```childFolder``` subfolder in ```demo``` was copied successfully. 
<div><pre>cp -r src/test/resources/demo/childFolder src/test/resources/newChildFolder
java -jar build/libs/CopyCheck-0.0.1.jar --source=$PWD/src/test/resources/demo/childFolder --srcFileList=test.list --SHA-256 --destination=src/test/resources/newChildFolder"
</div></pre>

### Result
<pre>
>> Valid << 

Hash                                                              Path
0263829989b6fd954f72baaf2fc64bc2e2f01d692d4de72986ea808f6e99813f  /mnt/code/java/CopyCheck/src/test/resources/newChildFolder/2

Summary: 1 of 1 are valid
</pre>

### Explanation
Here we used a <em>source</em> that was a child directory of the snapshot source, because we only copied a child directory.  The <em>destination</em> was similarly modified.  CopyCheck only parsed files that were children of ```./src/test/resources/demo/childFolder```, this allowed us to confirm the copy of ```childFolder``` to ```newChildFolder``` without creating a new snapshot.



