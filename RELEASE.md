## How to release

- Use the _setVersion_ script to apply the new version, e.g. "setVersion.bat 1.3.5".
- Push everything
- Go to github actions and trigger the workflow job, this will build a release draft to review.
- Manually publish the drafted release.
- Clear the RELEASE_NOTES.md afterwards to start with the next version.