# Simon's Notes

My notes as I work on RealmSpeak. 

## Environment
 
Mac Book Pro 
- SysVer: OS X 10.11.3
- Kernel: Darwin 15.3.0

## Creating the build file

```
    cd build
    ant -buildfile generate-build.xml
```

### Building all

```
    cd build
    ant
```

Output files are in `products` directory.

### Building Javadocs

```
    cd build
    ant javadoc-all-projects
```

Javadocs are generated in the `javadocs` directory. 
Open `javadocs/index.html` to view them.

The docs are also bundled in `products\javadoc.zip`. 


### .gitignore

- Removed duplicate entries
- Commented out `build` since we need that directory
- ..but added artifacts generated in the build directory by
    `generate-build` ant file.

