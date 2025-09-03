# Clean up script for MVVM structure

## Files to DELETE (duplicates in wrong locations):
1. presentation/ui/viewmodel/ChatViewModel.kt 
2. presentation/ui/utils/CustomQueries.kt
3. presentation/ui/viewmodel/ (entire folder)
4. presentation/ui/utils/ (entire folder)

## Correct structure should be:
```
app/recruit/collegebot/
├── presentation/
│   ├── viewmodel/
│   │   └── ChatViewModel.kt ✅ (KEEP)
│   └── ui/
│       ├── chat/
│       │   └── ChatPage.kt ✅ (KEEP)
│       ├── main/
│       ├── splash/
│       └── theme/
└── utils/
    └── CustomQueries.kt ✅ (KEEP)
```

After cleanup, you'll have a clean MVVM structure with no duplicates.
