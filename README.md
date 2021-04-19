# UPT Server Extension

## Table of contents

- Description
- How To Use
- References
- License
- Author Info

## Description

This project provides the backend for the Urban Performance and Suitability tools.

## Main Technologies Used

- Oskari
- Maven

## How To Use

This Oskari's server extension is intended to be used in an instance of oskari-server-v1.55.1, the oskari-server-v1.55.1 is available at [https://github.com/oskariorg/oskari-server/tree/1.55.1](https://github.com/oskariorg/oskari-server/tree/1.55.1)

### Compilation

1. Build the UPT GUI project, following the instrucctions available in the readme file of the repository [https://github.com/UPTechMX/UPT-GUI](https://github.com/UPTechMX/UPT-GUI)
2. Clone this repository with the command `git clone https://github.com/UPTechMX/UPT-Server-Extension`
3. Move to the newly created directory `UPT-Server-Extension`
4. To compile the extension, execute the following command `mvn clean install`, after the compilation finishes, a war file(`oskari-map.war`) can be found in the directory `UPT-Server-Extension/weebapp-map/target/`.

### Installation

1. To proceed with the installation, the file `oskari-map.war` must be uploaded in to the Oskari's server diretory. The Oskari server have one direcotry called `webapps` that is the place where the file must be uploaded.

2. Edit the file `oskari-ext.properties` located in the directory `resources`, the file have to be updated to register the addresses of the tools [UPT Distance Module](https://github.com/UPTechMX/UPT-Distance-Module) and the  [UPT UrbanPerformance](https://github.com/UPTechMX/UPT-UrbanPerformance)

3. The following lines must be added at the end of the `oskari-ext.properties` file. Just replace the variables(OSKARI_HOST,OSKARI_DATABASE_NAME,OSKARI_DATABASE_PORT,UPT_URBANPERFORMANCE_HOST,UPT_URBANPERFORMANCE_PORT,UPT_DISTANCE_MODULE_HOST,UPT_DISTANCE_MODULE_PORT) with the correct values

    ```
    up.db.URL=jdbc:postgresql://OSKARI_DATABASE_HOST:OSKARI_DATABASE_PORT/OSKARI_DATABASE_NAME
    up.db.password=OSKARI_PASSWORD
    up.db.user=OSKARI_USER

    upws.db.host=UPT_URBANPERFORMANCE_HOST
    upws.db.port=UPT_URBANPERFORMANCE_PORT
    stws.db.host=UPT_DISTANCE_MODULE_HOST
    stws.db.port=UPT_DISTANCE_MODULE_PORT
    ```

4. Finally to register the **UPT Server Extension** into Oskari, register it using the name `example`. The registration is done in the same file `oskari-ext.properties`.

    ```
    db.additional.modules=myplaces, userlayer, example
    ```

5. After all the previous steps, the only remaining step is to reboot the oskari server application so it will install the extension that have been registered.

## References

- For a detailed iformation about Oskaris intallation visit [https://www.oskari.org/documentation](https://www.oskari.org/documentation)

## License

- SPDX-License-Identifier: MIT
- Copyright (c) 2020 CAPSUS S.C.

## Author Info

CAPSUS S.C. Capital Sustentable (CAPSUS) is a mission-oriented consulting firm, based in Mexico City, specialized in sustainability issues in the energy, environmental and urban sectors. Since 2009, the firm has executed projects that promote urban sustainable development both in Mexico and abroad. CAPSUS aims at increasing the performance of our clients by designing and promoting public policies, business practices and social behaviors that facilitate sustainable development, where energy efficiency, environmental protection, social integration, and economic development are paramount. The services offered by CAPSUS are focused on empowering best practices in sustainability through different approaches. A bottom-up approach is taken with urban and environmental management projects and a top-down angle with research, development, and public policy implementation projects. This two-end approach ensures not only the implementation of the projects but also their impact on a bigger scale and the possibility of replication.
