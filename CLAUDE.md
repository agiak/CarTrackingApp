# CarTrackingApp — working notes for Claude

## Git identity (required)

Commits in this repository are authored by the repository owner, never by Claude.
Before the first commit of a session, set:

```bash
git config user.name  "agiak"
git config user.email "anastas.giakoumatos@gmail.com"
```

A session runs in a fresh container with its own clone, so this has to be set
again every session — the default identity there is `Claude <noreply@anthropic.com>`
and it will otherwise end up in the commit's author and committer fields.

Do **not** add `Co-Authored-By: Claude …` or `Claude-Session: …` trailers to
commit messages, and do not add generated-by footers to pull request
descriptions. The commit message should describe the change only.

This is about the author metadata written into the commit. The push itself
already goes to GitHub under the owner's account.
