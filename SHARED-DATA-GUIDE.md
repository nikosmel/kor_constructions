# Sharing Data Between Two Laptops - Complete Guide

## Overview
There are 4 ways to share the JSON database files between two laptops:

1. **Cloud Storage** (Easiest - Recommended) ⭐
2. **Shared Network Folder** (Office/Home Network)
3. **Manual Sync** (USB Drive)
4. **Network Database** (Advanced - Coming soon)

---

## Method 1: Cloud Storage (RECOMMENDED) ⭐

### How It Works:
- Both laptops sync with Dropbox/Google Drive/OneDrive
- Data files automatically sync in real-time
- No manual copying needed

### Setup Steps:

#### On Laptop 1 (Primary):

1. **Install Cloud Storage**
   - Choose one: [Dropbox](https://www.dropbox.com) | [Google Drive](https://drive.google.com) | [OneDrive](https://onedrive.live.com)
   - Sign in and let it sync

2. **Create Shared Folder**
   ```
   Example paths:
   - Dropbox:      C:\Users\YourName\Dropbox\KorConstructions-Data
   - Google Drive: C:\Users\YourName\Google Drive\KorConstructions-Data
   - OneDrive:     C:\Users\YourName\OneDrive\KorConstructions-Data
   ```

3. **Run Setup Script**
   - Double-click: `setup-cloud-sync.bat`
   - Enter your cloud folder path when prompted
   - Script will copy existing data and configure the app

4. **Update Run Script**
   - Now use: `run-jar-with-custom-data.bat` instead of `run-jar.bat`
   - This loads the custom data location

#### On Laptop 2 (Secondary):

1. **Install Same Cloud Storage**
   - Use the SAME account as Laptop 1
   - Wait for folder to sync completely

2. **Copy Application**
   - Copy the entire KorConstructions folder to Laptop 2

3. **Run Setup Script**
   - Double-click: `setup-cloud-sync.bat`
   - Enter the SAME cloud folder path
   - Example: `C:\Users\YourName\Dropbox\KorConstructions-Data`

4. **Start Application**
   - Double-click: `run-jar-with-custom-data.bat`

### ✅ Advantages:
- Automatic synchronization
- Works from anywhere
- No network setup needed
- Free (5GB-15GB usually enough)

### ⚠️ Important:
- Don't run the app on BOTH laptops at the exact same time
- Close the app on one laptop before opening on the other
- Wait a few seconds for cloud sync after closing

---

## Method 2: Shared Network Folder

### How It Works:
- One laptop shares a folder on the local network
- Other laptop connects to that shared folder
- Both laptops access the same files

### Setup Steps:

#### On Laptop 1 (Server - Shares the folder):

1. **Create Shared Folder**
   ```
   Example: C:\SharedData\KorConstructions
   ```

2. **Enable File Sharing**
   - Right-click folder → Properties
   - Go to "Sharing" tab → Click "Share"
   - Add user "Everyone" with "Read/Write" permissions
   - Click "Share"
   - Note the network path: `\\LAPTOP1\SharedData\KorConstructions`

3. **Configure Application**
   - Create file: `data-location.properties`
   - Add line: `app.data.directory=C:\\SharedData\\KorConstructions`
   - Save file

4. **Run Application**
   - Use: `run-jar-with-custom-data.bat`

#### On Laptop 2 (Client - Connects to shared folder):

1. **Map Network Drive**
   - Open File Explorer
   - Right-click "This PC" → "Map network drive"
   - Choose drive letter: Z:
   - Enter path: `\\LAPTOP1\SharedData\KorConstructions`
   - Check "Reconnect at sign-in"
   - Click "Finish"

2. **Configure Application**
   - Create file: `data-location.properties`
   - Add line: `app.data.directory=Z:\\`
   - Save file

3. **Run Application**
   - Use: `run-jar-with-custom-data.bat`

### ✅ Advantages:
- Fast (local network speed)
- No internet needed
- No cloud storage account needed

### ⚠️ Limitations:
- Both laptops must be on the same network
- Laptop 1 must be turned on when Laptop 2 wants to use it
- Don't run on both laptops simultaneously

---

## Method 3: Manual Sync (USB Drive)

### How It Works:
- Copy data folder to USB drive when done on Laptop 1
- Plug USB into Laptop 2 and copy back

### Setup Steps:

1. **On Laptop 1 (After working):**
   ```batch
   xcopy /E /I /Y data E:\KorConstructions-Backup\data
   ```
   (Replace E: with your USB drive letter)

2. **On Laptop 2 (Before working):**
   ```batch
   xcopy /E /I /Y E:\KorConstructions-Backup\data data
   ```

### ✅ Advantages:
- Simple
- No network needed
- Complete control

### ⚠️ Limitations:
- Manual process
- Easy to forget
- Risk of data conflicts

---

## Method 4: Local Network Database (Advanced)

### Coming Soon
For users who need real-time multi-user access, we can set up:
- PostgreSQL or MySQL database on one laptop
- Other laptops connect via network
- Supports simultaneous users

Contact for implementation if needed.

---

## Recommended Setup for Most Users:

### For Home/Office with Internet:
→ **Use Method 1 (Cloud Storage)**

Easiest setup:
```
1. Install Dropbox (free 2GB is enough)
2. Run setup-cloud-sync.bat on both laptops
3. Use run-jar-with-custom-data.bat to start
```

### For Office Network without Internet:
→ **Use Method 2 (Shared Network Folder)**

Best for:
- Both computers on same network
- No internet restrictions
- Fast local access

### For Separate Locations (Different offices):
→ **Use Method 1 (Cloud Storage)**

Cloud storage works anywhere with internet.

---

## File Synchronization Rules:

### ⚠️ CRITICAL: Never Run on Both Laptops Simultaneously!

**Correct Workflow:**
1. Close app on Laptop 1
2. Wait 10 seconds (cloud sync)
3. Open app on Laptop 2
4. Work on Laptop 2
5. Close app on Laptop 2
6. Wait 10 seconds
7. Can now open on Laptop 1

**Why?**
- Both laptops writing to same files = data corruption
- JSON files are not designed for simultaneous access
- Cloud/network sync needs time to complete

---

## Backup Strategy:

Even with shared data, maintain backups:

### Automatic Backup Script:
```batch
@echo off
set BACKUP_DIR=C:\Backups\KorConstructions
set DATE=%date:~-4,4%%date:~-7,2%%date:~-10,2%

mkdir "%BACKUP_DIR%\%DATE%"
xcopy /E /I /Y data "%BACKUP_DIR%\%DATE%\data"

echo Backup completed: %BACKUP_DIR%\%DATE%
```

Save as: `backup-data.bat`

Run weekly or before important updates.

---

## Troubleshooting:

### Problem: Data not syncing between laptops
**Solution:**
- Check cloud storage is actually syncing (look for green checkmarks)
- Wait longer (large files take time)
- Check internet connection
- Restart cloud storage app

### Problem: "File in use" error
**Solution:**
- Close app on other laptop
- Wait 30 seconds
- Try again

### Problem: Lost data
**Solution:**
- Check cloud storage website for older versions
- Restore from backup folder
- Check Windows "Previous Versions" (right-click data folder)

---

## Quick Reference:

### Files You Need:
- `setup-cloud-sync.bat` - Configure cloud location
- `run-jar-with-custom-data.bat` - Start with custom location
- `data-location.properties` - Stores custom path (auto-created)

### After Initial Setup:
Just use: `run-jar-with-custom-data.bat` to start the app!

---

## Support:

For questions about setup:
- Check that cloud folder paths are IDENTICAL on both laptops
- Verify data-location.properties file exists
- Test by creating a test customer on Laptop 1, sync, then check on Laptop 2
