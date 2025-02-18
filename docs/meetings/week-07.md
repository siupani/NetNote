| Key          | Value                                                                            |
| ------------ | -------------------------------------------------------------------------------- |
| Date         | 15-01-2025                                                                       |
| Time         | 13:45                                                                            |
| Location     | Drebbelweg PC3                                                                   |
| Chair        | Melle Moerkerk                                                                   |
| Minute Taker | Luca Bledea Floruta                                                              |
| Attendees    | Victor Badescu, Luca Bledea Floruta, Melle Moerkerk, Sergiu Tiron, Stepan Urumov |

### Agenda Items

1. **Opening by Chair** (1 min)
2. **Check-in: How is everyone doing & what has everyone done the past week?** (1 min)
3. **Announcements by the team** (2 min)
4. **Approval of the agenda & last week's minutes** (2 min)
6. **Announcements by the TA** (7 min)
7. **Presentation of the current app to TA** (5 min)
8. **Talking Points** (12 min)
    - **Monday meeting retrospective** (4 min)
        - Meeting attendance, communication
        - Task division
    - **Project state** (5 min)
        - Formative feedback (what to fix; status update)
        - What do we still want to achieve before the code freeze?
    - **Planning for this & next week** (3 min)
9. **Summarize action points** (3 min)
10. **Questions Round** (5 min)
11. **Feedback Round** (2 min)
12. **Planned Meeting Duration vs Actual Duration** (1 min)
13. **Closure** (1 min)

Estimated time: 42 min


### Minute-Taker Notes:

### **1.Check-In: Team Updates**
**Stepan:**
- Worked on the JSON file. The collection is stored in the config file.
- **Worry:** Storing collections on multiple servers leads to duplication (e.g., same ID for different collections).

**Sergiu:**
- Fixed a bug with the default collection.
- **Remaining Issue:** Problems with the display when creating a new collection.

**Luca:**
- Shortcuts for the essential functions
- Persisting the preferred language of the user

**Victor:**
- Implemented changes for collections and improved the GUI.
- Synced collection with the server field and status field.
- **Remaining Issue:** Default collection is not displayed in the list.

**Melle:**
- Worked on language functionality (flag icons for translation).
- Created a new MR to translate menus.
- **Remaining Work:** Some translations are still missing.

### **2.Announcements by the TA**
- Knockout criteria from week 6 were sent incorrectly and will be resent.
- Warnings for incomplete (full) criteria will be issued next week.
- Feedback for features has been released.
- Saving additional features in the config file is acceptable.

### **3.Monday Meeting Feedback**
- **Melle:** Not happy with the way the members with the way the meeting was conducted(he was waiting on Discord).
- **Sergiu:** Lack of response in group chats delayed coordination.
- **Stepan:** The team used a projector, which complicated online participation.
- **Consensus:** Improvements are needed for smoother communication.

### **4.Question Round Summary**
- **Victor:** When should collections be updated on the server?
- **TA:** Will share details from Mattermost.
- **TA:** The app should have only one choice box for the language
- **Consensus:** Choose the one from the setting menu
- **TA:** Occasional delays in MR reviews are acceptable, but avoid frequent long delays.
- **TA:** You can add reviews for MRs even after they are approved/merged.
- **TA:** If a note is created on one client, it should appear on another after a refresh.
- **TA:** Feedback suggests using `@Controller` instead of `@RestController`, but further investigation is needed.
- **TA:** Formative feedback is stricter than summative feedback.

### **Before Code Freeze (24th of Jan, Friday, 12 PM)**
- **Stepan:**
   - Proposes polishing current issues, milestones, and MRs by next week’s meeting.
   - Suggests combining efforts next week to implement a new feature.
   - New features should not be added after Wednesday.
   - Suggests fixing issues and improving milestone tracking this week.

### **Conclusion**
- Review the backlog and address issues by Monday.
- Focus on embedding files and polishing the code for the upcoming code freeze.
- MR's to be approved only after commenting and reviewing them