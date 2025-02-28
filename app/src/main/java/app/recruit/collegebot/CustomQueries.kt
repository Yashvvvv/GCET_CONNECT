package com.example.collegebot



val customQueries = mapOf(
    // Academic Calendar & Schedule
    "When does the semester start" to "The academic calendar is available on I-Cloud. Generally:\n- Odd Semester: July/August\n- Even Semester: January\nCheck your department notice board for exact dates.",
    "What are class timings" to "Classes run from 9:00 AM to 4:30 PM with:\n- 1st period: 9:00-9:55 AM\n- Lunch break: 12:40-1:30 PM\n- Last period: 3:35-4:30 PM",
    
    // Department Information
    "List of departments in GCET" to "GCET has the following departments:\n- Computer Science & Engineering\n- Information Technology\n- Electronics & Communication\n- Electrical Engineering\n- Mechanical Engineering\n- Civil Engineering\nLocated in different blocks (A, B, C, D)",
    "Where is CSE department" to "CSE Department is located in:\n- Main Office: B Block, 2nd Floor\n- Labs: B Block (1st & 2nd Floor)\n- Faculty Rooms: B Block (All floors)",
    
    // Infrastructure
    "College facilities" to "GCET facilities include:\n- Central Library (A Block)\n- Computer Labs\n- Sports Complex\n- Cafeteria\n- Medical Room\n- Auditorium\n- Innovation Labs\n- Seminar Halls",
    "Where is the library" to "Central Library is in A Block:\n- Ground + First Floor\n- Timing: 8:30 AM - 5:00 PM\n- Digital Section available\n- Reading halls on both floors",
    
    // Academic Resources
    "How to access study material" to "Study materials available through:\n1. I-Cloud Portal\n2. Department Library\n3. Faculty Handouts\n4. Digital Library Access\nContact your class coordinator for access.",
    "Where to get previous year papers" to "Previous year papers available at:\n1. Department Library\n2. I-Cloud Portal\n3. Class Coordinator\n4. Subject Faculty",

    // Examination
    "Exam pattern" to "Examinations include:\n1. Sessionals (30 marks)\n- Two sessionals per semester\n- Best of two considered\n2. Finals (70 marks)\n- End semester examination",
    "Supplementary exam rules" to "Supplementary exams:\n- Conducted for failed subjects\n- Usually in July/August\n- Registration required\n- Fees applicable per subject",
    
    // Placements
    "Placement cell location" to "Training & Placement Cell:\n- Location: D Block, Ground Floor\n- Timing: 9:00 AM - 5:00 PM\n- Contact: placement.gcet@galgotias.edu",
    "Companies visiting campus" to "Regular recruiters include:\n- IT Giants (TCS, Infosys, Wipro)\n- Product Companies\n- Core Engineering\nCheck T&P notice board for schedule",
    
    // Administrative
    "Important contact numbers" to "Key contacts:\n- Reception: 0120-XXXXXXX\n- Academic Office: 0120-XXXXXXX\n- Examination Cell: 0120-XXXXXXX\n- Emergency: 0120-XXXXXXX",
    "How to get documents" to "For official documents visit:\n- Academic Section (D Block)\n- Timing: 10:00 AM - 4:00 PM\n- Required: College ID\n- Processing time: 2-3 days",
    
    // Transportation
    "College bus service" to "College bus service:\n- Multiple routes covering NCR\n- Pickup points list on website\n- Bus pass from Transport Office\n- Contact: transport.gcet@galgotias.edu",
    "Bus pass procedure" to "For bus pass:\n1. Visit Transport Office (Gate 2)\n2. Fill application form\n3. Submit with ID proof\n4. Pay semester charges\nTiming: 9:30 AM - 3:30 PM",
    
    // Hostel
    "Hostel facilities" to "GCET hostels offer:\n- Separate boys/girls blocks\n- Wi-Fi enabled\n- Mess facility\n- 24x7 security\n- Medical facility\nContact hostel warden for details",
    "Hostel admission process" to "Hostel admission steps:\n1. Apply through I-Cloud\n2. Submit documents\n3. Pay fees\n4. Room allocation\nContact hostel office: hostel.gcet@galgotias.edu",
    
    // Sports & Recreation
    "Sports facilities" to "Sports facilities include:\n- Cricket ground\n- Basketball court\n- Volleyball court\n- Indoor games room\n- Gym\nLocation: Behind B Block",
    "How to join sports team" to "Join sports teams:\n1. Contact Sports Officer\n2. Attend trials\n3. Selection based on performance\nOffice: Near Sports Complex",

    // Admission Queries
    "How can I apply for admission in gcet college" to "You can apply through our official website. Go to the admissions page, fill out the form, upload your documents, and submit the application fee. Deadlines and details can be found on the same page. ADMISSION CELL(D BLOCK GF, GCET CAMPUS 1).",

    // Eligibility Criteria
    "What are the eligibility criteria for admission to the BTech program" to "For the BTech program, you need at least 60% in your 12th grade with Physics, Chemistry, and Mathematics. You also need to clear the entrance exam [JEE OR UPSEE].",

    // Course Registration
    "How can I register for my courses for the next semester" to "You can register for courses through I CLOUD website. Login, select your program, and choose the available courses under the 'Course Registration' section.",

    // Examination Schedule
    "Where can I find the examination schedule" to "The examination schedule is available on the college website under the 'Examinations' tab. You will receive important updates via your class coordinator.",

    // Attendance Requirements
    "What is the minimum attendance required to sit for exams" to "You need a minimum of 75% attendance to be eligible for final exams. Attendance details can be checked in the student portal (I CLOUD).",

    // Additional Queries (Add more here)
    "How do I pay my semester fees" to "Semester fees can be paid online via the student portal under the 'FEE' section. Payment methods include debit/credit cards, net banking, or UPI.",

    // Add all the other queries from the provided text in the same format
    "What are the college timings" to "The college operates from 9 AM to 5 PM.",
    "Who is the director of galgotias college" to "The director of Galgotias College is Mr. Vikram Bali.",
    "Nearest Metro Station to galgotias" to "The nearest Metro Station is Knowledge Park 2, Aqua Line.",
    "Where is Director's office" to "The Director's office is located in A block ground floor of the college.",

    // Bot's Name Handling (Custom Responses for "What is your name?")
    "What is your name" to "My name is GCET Connect.",
    "Tell me your name" to "My name is GCET Connect.",
    "What should I call you" to "You can call me GCET Connect.",
    "Who are you?" to "I am GCET Connect, your virtual assistant.",
    "What do you go by?" to "You can call me GCET Connect.",
    "What’s your name" to "My name is GCET Connect.",
    "How should I address you" to "You can address me as GCET Connect.",
    "What is your title" to "I am GCET Connect, your assistant.",

    // Club Related Queries
    "What clubs are there in college" to "GCET has several active clubs including:\n- Coding Club\n- Cultural Club\n- Sports Club\n- Literary Club\n- Photography Club\nYou can find more information about clubs at the Student Activity Center.",
    "How to join college clubs" to "To join college clubs, visit the Student Activity Center or contact the respective club coordinators. Most clubs conduct recruitment drives at the start of each semester.",
    "Tell me about coding club" to "The Coding Club at GCET organizes coding competitions, workshops, and hackathons. They meet regularly to discuss programming concepts and work on projects.",
    "Cultural club activities" to "The Cultural Club organizes events like:\n- Annual Cultural Fest\n- Dance competitions\n- Music performances\n- Theatre workshops\nThey practice in the college auditorium.",

    // Assignment Related Queries
    "How to submit assignments" to "Assignments can be submitted in two ways:\n1. Online: Through I-Cloud portal under 'Assignments' section\n2. Offline: Submit hard copy to respective faculty during class hours",
    "Assignment format" to "Standard assignment format includes:\n- College Header\n- Student Details (Name, Roll No, Branch)\n- Subject & Topic\n- Faculty Name\nDownload template from I-Cloud portal.",
    "Where to make assignments" to "You can create assignments at:\n1. College Computer Lab\n2. Library's Digital Section\n3. Stationery shop for printing/binding",
    "Assignment deadline" to "Assignment deadlines are set by individual faculty members. Check I-Cloud portal or contact your subject teacher for specific dates.",

    // Stationery Related Queries
    "Where is college stationery shop" to "The college stationery shop is located at:\n- Main shop: Ground Floor, B Block\n- Branch shop: Near Library",
    "Stationery shop timing" to "Stationery shop operates from 9:00 AM to 5:00 PM on all working days.",
    "What items are available in stationery" to "College stationery shop provides:\n- Assignment sheets\n- Lab manuals\n- Drawing supplies\n- Printing services\n- Basic stationery items\n- Binding services",
    "Assignment sheets price" to "Assignment sheets pricing:\n- Single sheet: ₹2\n- Full assignment bundle: ₹20\n- Printing: ₹5/page\n- Binding: ₹30-50",

    // Lab Manual Queries
    "Where to get lab manuals" to "Lab manuals are available at:\n1. College stationery shop\n2. Digital copies on I-Cloud\n3. Department office",
    "Lab manual format" to "Lab manuals must follow college format with:\n- Experiment title\n- Aim\n- Theory\n- Procedure\n- Observations\n- Result",

    // Practical File Queries
    "How to make practical file" to "Practical files should:\n1. Use college-approved formats\n2. Include all experiments\n3. Get regular signatures from faculty\n4. Be properly indexed\nGet materials from college stationery.",
    
    // Project Related
    "Where to do college projects" to "College projects can be done at:\n1. College Innovation Lab (C Block)\n2. Department Labs\n3. Library's Research Section\nBook slots through department coordinator.",
)
