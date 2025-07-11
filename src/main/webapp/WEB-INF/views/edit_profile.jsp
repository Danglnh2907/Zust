<%@ page import="model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- Edit Profile Modal -->
<div class="modal fade" id="editProfileModal" tabindex="-1" aria-labelledby="editProfileModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="editProfileModalLabel">Edit Profile</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form id="editProfileForm" enctype="multipart/form-data">
                    <div class="mb-3">
                        <label for="editFullname" class="form-label">Full Name</label>
                        <input type="text" class="form-control" id="editFullname" name="fullname" required>
                    </div>
                    <div class="mb-3">
                        <label for="editUsername" class="form-label">Username</label>
                        <input type="text" class="form-control" id="editUsername" name="username" readonly>
                    </div>
                    <div class="mb-3">
                        <label for="editEmail" class="form-label">Email</label>
                        <input type="email" class="form-control" id="editEmail" name="email" readonly>
                    </div>
                    <div class="mb-3">
                        <label for="editPhone" class="form-label">Phone</label>
                        <input type="text" class="form-control" id="editPhone" name="phone">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Gender</label>
                        <div>
                            <input type="radio" id="editMale" name="gender" value="true">
                            <label for="editMale" class="me-3">Male</label>
                            <input type="radio" id="editFemale" name="gender" value="false">
                            <label for="editFemale">Female</label>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="editDob" class="form-label">Date of Birth</label>
                        <input type="date" class="form-control" id="editDob" name="dob">
                    </div>
                    <div class="mb-3">
                        <label for="editBio" class="form-label">Bio</label>
                        <textarea class="form-control" id="editBio" name="bio" rows="3"></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="editAvatar" class="form-label">Avatar</label>
                        <input type="file" class="form-control" id="editAvatar" name="avatarFile" accept="image/*">
                        <input type="hidden" id="currentAvatar" name="avatar">
                    </div>
                    <div class="mb-3">
                        <label for="editCoverImage" class="form-label">Cover Image</label>
                        <input type="file" class="form-control" id="editCoverImage" name="coverImageFile" accept="image/*">
                        <input type="hidden" id="currentCoverImage" name="coverImage">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                <button type="button" class="btn save-btn" id="saveProfileChanges">Save changes</button>
            </div>
        </div>
    </div>
</div>
