using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace LKS_ITSSA_2025.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class PictureImageController : ControllerBase
    {
        private readonly IWebHostEnvironment _environment;

        public PictureImageController(IWebHostEnvironment environment)
        {
            _environment = environment;
        }

        [RequestFormLimits(MultipartBodyLengthLimit = 52428800)] // 50MB
        [RequestSizeLimit(52428800)] // 50MB
        [HttpPost]
        public IActionResult imgUser(IFormFile file)
        {
            try
            {
                if (file == null || file.Length == 0)
                {
                    return BadRequest("File tidak ditemukan");
                }

                string[] allowedExtensions = { ".jpg", ".jpeg", ".png" };
                string fileExtension = Path.GetExtension(file.FileName).ToLower();

                if (!allowedExtensions.Contains(fileExtension))
                {
                    return BadRequest("Format file tidak didukung. Gunakan .jpg, .jpeg, atau .png");
                }

                // Cek apakah WebRootPath null, jika iya gunakan ContentRootPath
                string rootPath = _environment.WebRootPath ?? _environment.ContentRootPath;

                string uploadsFolder = Path.Combine("wwwroot", "uploads");

                // Buat Folder Jika belum ada
                if (!Directory.Exists(uploadsFolder))
                {
                    Directory.CreateDirectory(uploadsFolder);
                }

               /* string timeStamp = DateTime.Now.ToString("yyyyMMddHHmmss");*/
             /*   string originalFileName = Path.GetFileNameWithoutExtension(file.FileName);*/
                string fileName = file.FileName;
                string filePath = Path.Combine(uploadsFolder, fileName);

                using (var fileStream = new FileStream(filePath, FileMode.Create))
                {
                    file.CopyTo(fileStream);
                }

                string fileUrl = $"{Request.Scheme}://{Request.Host}/uploads/{fileName}";

                return Ok(new
                {
                    message = "File berhasil diupload",
                    url = fileUrl
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Internal server error: {ex.Message}");
            }
        }

    }
}
