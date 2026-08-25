# Versiones y Configuración del Entorno

SO: Windows 11[cite: 1]

---

## 1. Extensiones de VS Code

Para instalar todo de un jalón en la terminal:

```bash
code --install-extension ecmel.vscode-html-css --install-extension ms-azuretools.vscode-containers --install-extension ms-azuretools.vscode-docker --install-extension ms-dotnettools.csdevkit --install-extension ms-dotnettools.csharp --install-extension ms-dotnettools.vscode-dotnet-runtime --install-extension ms-vscode.live-server --install-extension redhat.java --install-extension vscjava.vscode-gradle --install-extension vscjava.vscode-java-debug --install-extension vscjava.vscode-java-dependency --install-extension vscjava.vscode-java-pack --install-extension vscjava.vscode-java-test --install-extension vscjava.vscode-maven
```
[cite: 1]

### Lista de versiones usadas:
* `ecmel.vscode-html-css@2.0.14`[cite: 1]
* `ms-azuretools.vscode-containers@2.5.0`[cite: 1]
* `ms-azuretools.vscode-docker@2.0.0`[cite: 1]
* `ms-dotnettools.csdevkit@3.20.199`[cite: 1]
* `ms-dotnettools.csharp@2.140.9`[cite: 1]
* `ms-dotnettools.vscode-dotnet-runtime@3.1.0`[cite: 1]
* `ms-vscode.live-server@0.4.20`[cite: 1]
* `redhat.java@1.55.0`[cite: 1]
* `vscjava.vscode-gradle@3.18.0`[cite: 1]
* `vscjava.vscode-java-debug@0.59.0`[cite: 1]
* `vscjava.vscode-java-dependency@0.27.6`[cite: 1]
* `vscjava.vscode-java-pack@0.31.1`[cite: 1]
* `vscjava.vscode-java-test@0.46.0`[cite: 1]
* `vscjava.vscode-maven@0.45.3`[cite: 1]

---

## 2. MariaDB 11.8.3

Instalación en Windows 11 con el instalador oficial MSI:[cite: 1]

1. Descargar el `.msi` de la versión **11.8.3** desde la página oficial de MariaDB.[cite: 1]
2. Abrir el instalador y darle a *Next*.[cite: 1]
3. Aceptar la licencia y dejar o cambiar la ruta de instalación.[cite: 1]
4. Poner contraseña al usuario `root` (activar conexiones remotas si hace falta).[cite: 1]
5. Marcar la casilla **Install as Service** para que corra de fondo con el nombre por defecto (`MariaDB`).[cite: 1]
6. Darle a **Install** y finalizar.[cite: 1]