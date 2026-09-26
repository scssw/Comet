//go:build windows && with_purego

package libcore

import (
	"os"
	"path/filepath"
	"strings"
	"unsafe"

	"github.com/sagernet/cronet-go"
	"golang.org/x/sys/windows"
)

var dummyModuleAnchor int

func init() {
	loadCronetWindows()
}

func loadCronetWindows() {
	var candidates []string

	// 1. Directory of the loaded husicore.dll
	var hModule windows.Handle
	err := windows.GetModuleHandleEx(
		windows.GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS|windows.GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
		(*uint16)(unsafe.Pointer(&dummyModuleAnchor)),
		&hModule,
	)
	if err == nil && hModule != 0 {
		var buf [windows.MAX_PATH]uint16
		n, err := windows.GetModuleFileName(hModule, &buf[0], uint32(len(buf)))
		if err == nil && n > 0 {
			dllDir := filepath.Dir(windows.UTF16ToString(buf[:n]))
			candidates = append(candidates, dllDir)
		}
	}

	// 2. Directory of current executable
	if exePath, err := os.Executable(); err == nil {
		candidates = append(candidates, filepath.Dir(exePath))
	}

	// 3. Well-known install paths on Windows
	if localAppData := os.Getenv("LOCALAPPDATA"); localAppData != "" {
		candidates = append(candidates, filepath.Join(localAppData, "Programs", "Comet"))
		candidates = append(candidates, filepath.Join(localAppData, "Programs", "Husi"))
	}
	if progFiles := os.Getenv("ProgramFiles"); progFiles != "" {
		candidates = append(candidates, filepath.Join(progFiles, "husi"))
		candidates = append(candidates, filepath.Join(progFiles, "Comet"))
	}
	if progFiles86 := os.Getenv("ProgramFiles(x86)"); progFiles86 != "" {
		candidates = append(candidates, filepath.Join(progFiles86, "husi"))
		candidates = append(candidates, filepath.Join(progFiles86, "Comet"))
	}

	// 4. Current working directory
	if cwd, err := os.Getwd(); err == nil {
		candidates = append(candidates, cwd)
	}

	// 5. System PATH directories
	if pathEnv := os.Getenv("PATH"); pathEnv != "" {
		candidates = append(candidates, filepath.SplitList(pathEnv)...)
	}

	// Deduplicate candidates
	seen := make(map[string]bool)
	for _, dir := range candidates {
		dir = strings.TrimSpace(dir)
		if dir == "" || seen[dir] {
			continue
		}
		seen[dir] = true

		dllPath := filepath.Join(dir, "libcronet.dll")
		if info, err := os.Stat(dllPath); err == nil && !info.IsDir() {
			// Found libcronet.dll!
			// Add its directory to DLL search path and PATH env
			_ = windows.SetDllDirectory(dir)
			currentPath := os.Getenv("PATH")
			_ = os.Setenv("PATH", dir+string(os.PathListSeparator)+currentPath)

			// Load the library directly into cronet-go
			if err := cronet.LoadLibrary(dllPath); err == nil {
				return
			}
		}
	}
}
