# Kor Constructions - Windows Setup Guide

## Quick Start (3 Steps)

### Step 1: Install Prerequisites

**Java 17 or higher** (Required)
1. Download: https://adoptium.net/temurin/releases/
2. Choose: Windows x64, JDK 17 or 21
3. Run installer and follow instructions
4. Verify: Open Command Prompt and type `java -version`

**Maven** (Optional - only needed if not using JAR method)
1. Download: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Maven`
3. Add to PATH:
   - Search Windows for "Environment Variables"
   - Edit "Path" system variable
   - Add: `C:\Program Files\Maven\bin`
4. Verify: Open new Command Prompt and type `mvn -version`

---

## Step 2: Choose Your Method

### Method A: Using JAR File (Recommended - Easier!)

**Build once (requires Maven):**
```
1. Double-click: build-jar.bat
2. Wait for build to complete
```

**Run anytime (only needs Java):**
```
Double-click: run-jar.bat
```

The application will start and open in your browser automatically!

### Method B: Using Maven (Development Mode)

```
Double-click: start-app-and-open-browser.bat
```

The application will start and open in your browser automatically!

---

## Step 3: Create Desktop Shortcut

### For JAR Method (Recommended):

1. **Right-click** on `run-jar.bat`
2. Choose **"Send to" → "Desktop (create shortcut)"**
3. **Right-click** the desktop shortcut → **Properties**
4. Click **"Change Icon"**
5. Browse to choose an icon (or use default)
6. In "Start in" field, paste the full path to your project folder:
   ```
   C:\Users\YourName\Documents\KorConstructions
   ```
7. Click **OK**

### For Maven Method:

Same steps as above, but use `start-app-and-open-browser.bat` instead.

---

## Creating a Custom Icon (Optional)

1. Find an icon image you like (.ico file)
2. Save it as `app-icon.ico` in the project folder
3. Right-click your desktop shortcut → Properties → Change Icon
4. Click "Browse" and select `app-icon.ico`
5. Click OK

---

## Usage

### Starting the Application:
- **Double-click** the desktop shortcut
- Wait 10-15 seconds for startup
- Browser opens automatically to `http://localhost:8080`

### Stopping the Application:
- Close the black command window that opened
- OR press Ctrl+C in that window

### Data Location:
All your data is saved in:
```
C:\Users\YourName\Documents\KorConstructions\data\
```

Files:
- `customers.json` - All customer data
- `receipts.json` - All receipt data
- `payments.json` - All payment data

**IMPORTANT:** Backup this `data` folder regularly!

---

## Troubleshooting

### "Java is not installed" error:
- Install Java from: https://adoptium.net/
- After installation, restart your computer

### "Port 8080 already in use":
- Another application is using port 8080
- Close any other Java applications
- OR restart your computer

### Application doesn't open in browser:
- Manually open browser and go to: `http://localhost:8080`

### JAR file not found:
- Run `build-jar.bat` first
- Make sure it completes successfully

---

## Sharing with Other Computers

To use this application on another Windows computer:

1. **Build the JAR file first:**
   - Run `build-jar.bat` on your current computer

2. **Copy these items to the other computer:**
   ```
   - target\kor-constructions-app-1.0.0.jar
   - run-jar.bat
   - data\ folder (your data)
   ```

3. **On the other computer:**
   - Install Java 17 or higher
   - Double-click `run-jar.bat`

---

## Advanced: Running as Windows Service (Always On)

If you want the application to start automatically when Windows starts:

1. Download NSSM (Non-Sucking Service Manager):
   - https://nssm.cc/download

2. Extract and run as Administrator:
   ```
   nssm install KorConstructions
   ```

3. Configure:
   - Path: `C:\Program Files\Java\jdk-17\bin\java.exe`
   - Arguments: `-jar C:\Users\YourName\Documents\KorConstructions\target\kor-constructions-app-1.0.0.jar`
   - Startup directory: `C:\Users\YourName\Documents\KorConstructions`

4. Start service:
   ```
   nssm start KorConstructions
   ```

---

## Support

For issues or questions:
- Check the console window for error messages
- Verify Java version: `java -version`
- Make sure no other application uses port 8080
- Try restarting your computer
