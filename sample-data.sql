-- Sample Data for Demo/Testing
-- Run after services have started and created tables

-- ============================================
-- USER SERVICE DATABASE
-- ============================================
USE jira_user_db;

-- Note: Users should be created via API to get proper password hashing
-- This is just reference data for categories, etc.

-- Sample categories can be added via API after organization creation

-- ============================================
-- ISSUE SERVICE DATABASE
-- ============================================
USE jira_issue_db;

-- Categories (after organization with ID=1 is created)
INSERT INTO categories (organization_id, name, description, active) VALUES
(1, 'Hardware', 'Hardware related issues', true),
(1, 'Software', 'Software and application issues', true),
(1, 'Network', 'Network connectivity issues', true),
(1, 'Access', 'Access and permissions requests', true),
(1, 'General', 'General inquiries', true);

-- Sub-categories for Hardware
INSERT INTO sub_categories (category_id, name, description, active)
SELECT id, 'Laptop', 'Laptop issues', true FROM categories WHERE name = 'Hardware' AND organization_id = 1
UNION ALL
SELECT id, 'Desktop', 'Desktop computer issues', true FROM categories WHERE name = 'Hardware' AND organization_id = 1
UNION ALL
SELECT id, 'Printer', 'Printer issues', true FROM categories WHERE name = 'Hardware' AND organization_id = 1
UNION ALL
SELECT id, 'Monitor', 'Monitor/Display issues', true FROM categories WHERE name = 'Hardware' AND organization_id = 1;

-- Sub-categories for Software
INSERT INTO sub_categories (category_id, name, description, active)
SELECT id, 'Operating System', 'OS related issues', true FROM categories WHERE name = 'Software' AND organization_id = 1
UNION ALL
SELECT id, 'Application', 'Application software issues', true FROM categories WHERE name = 'Software' AND organization_id = 1
UNION ALL
SELECT id, 'License', 'Software licensing', true FROM categories WHERE name = 'Software' AND organization_id = 1;

-- Sub-categories for Network
INSERT INTO sub_categories (category_id, name, description, active)
SELECT id, 'WiFi', 'WiFi connectivity', true FROM categories WHERE name = 'Network' AND organization_id = 1
UNION ALL
SELECT id, 'VPN', 'VPN access issues', true FROM categories WHERE name = 'Network' AND organization_id = 1
UNION ALL
SELECT id, 'Internet', 'Internet connectivity', true FROM categories WHERE name = 'Network' AND organization_id = 1;

-- Sub-categories for Access
INSERT INTO sub_categories (category_id, name, description, active)
SELECT id, 'New User', 'New user account creation', true FROM categories WHERE name = 'Access' AND organization_id = 1
UNION ALL
SELECT id, 'Password Reset', 'Password reset requests', true FROM categories WHERE name = 'Access' AND organization_id = 1
UNION ALL
SELECT id, 'Permissions', 'Permission/access changes', true FROM categories WHERE name = 'Access' AND organization_id = 1;

-- Sample Support Groups (after organization created)
INSERT INTO support_groups (organization_id, name, description, level, active, created_at, updated_at) VALUES
(1, 'L1 Support Team', 'First level support - General inquiries and basic troubleshooting', 'L1', true, NOW(), NOW()),
(1, 'L2 Technical Team', 'Second level support - Advanced technical issues', 'L2', true, NOW(), NOW()),
(1, 'L3 Specialist Team', 'Expert level support - Complex system issues', 'L3', true, NOW(), NOW()),
(1, 'Network Team', 'Specialized network support', 'L2', true, NOW(), NOW()),
(1, 'Security Team', 'Security and access management', 'L2', true, NOW(), NOW());

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify categories
SELECT 
    c.name AS category,
    COUNT(sc.id) AS subcategory_count
FROM categories c
LEFT JOIN sub_categories sc ON c.id = sc.category_id
WHERE c.organization_id = 1
GROUP BY c.id, c.name;

-- Verify groups
SELECT name, level, description 
FROM support_groups 
WHERE organization_id = 1;

-- ============================================
-- CLEANUP (if needed)
-- ============================================

-- To remove sample data:
-- DELETE FROM sub_categories WHERE category_id IN (SELECT id FROM categories WHERE organization_id = 1);
-- DELETE FROM categories WHERE organization_id = 1;
-- DELETE FROM support_groups WHERE organization_id = 1;
