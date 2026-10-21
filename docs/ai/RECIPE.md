# Android Transcription Widget

## Overview

Embed live transcription UI in an Android RTC app so users can follow captions during a call.

Repository: [AgoraIO-Community/TranscriptionWidget-Android](https://github.com/AgoraIO-Community/TranscriptionWidget-Android)

## Prerequisites and Setup

1. Open the repository in Android Studio.
2. Copy `app/src/main/assets/server_config_example.json` to `app/src/main/assets/server_config.json`. Fill the server URL, test address/port, `appId` and authentication fields for a compatible transcription backend.
3. Sync and run the app, selecting the configured server and languages.
4. For library integration, use the `transcription-widget` module or the Maven dependency documented in the README. Feed the widget the RTC stream-message data and UID.

## What To Check

- Confirm interim and final transcript text render correctly.
- Check translated text, transcript history and the clear action.
- Stop/restart transcription and ensure messages are not duplicated.

## Integration Notes

The widget displays transcription output; it does not provision or start an STT backend on its own. A compatible, reachable service must be configured.

Keep App Certificates, customer secrets and privileged token generation on a trusted backend for production. Use only your own test credentials while configuring historical demos; do not commit them. Preserve the sample's existing dependency versions and lifecycle until a separate migration is tested.

## Source References

- [README.md](../../README.md)
- [app/src/main/assets/server_config_example.json](../../app/src/main/assets/server_config_example.json)
- [transcription-widget/build.gradle](../../transcription-widget/build.gradle)

## Validation Status

This recipe was checked against the repository's source and setup documents. The platform build and live service/device flow have not been run as part of this documentation change. Complete the checks above in an environment with the required SDKs, hardware and service access before treating it as a validated deployment.

## Implementation Prompt

```text
You are working in AgoraIO-Community/TranscriptionWidget-Android. Use docs/ai/RECIPE.md and its linked source files to adapt the Android Transcription Widget reference to this project.

Start by inspecting the actual sample directory and configuration described above. Resolve its listed missing prerequisites before claiming it is runnable. Preserve the existing architecture and compatible dependency versions. Implement the recipe's feature-specific flow, permissions and resource cleanup. Use a trusted backend for privileged credentials and tokens in production.

Run the repository-specific build or launch flow, then perform each What To Check item. Report the commands run, the observed results and anything blocked by service access, hardware or incomplete sample code. Do not claim live validation from a documentation or catalog build alone.
```
