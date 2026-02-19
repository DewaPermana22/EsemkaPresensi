using LKS_ITSSA_2025.Models;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.Extensions.FileProviders;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers().AddJsonOptions(x => x.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles);
builder.Services.AddDbContext<EsensiAppContext>();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(jwt =>
{
    jwt.TokenValidationParameters = new TokenValidationParameters
    {
        ClockSkew = TimeSpan.Zero,
        ValidateIssuer = false,
        ValidateAudience = false,
        RequireExpirationTime = true,
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(builder.Configuration["token"]!))
    };
});

builder.Services.AddAuthorization();


builder.Services.AddSwaggerGen(sw =>
{
    sw.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "EsensiApp - Esemka Presensi ",
        Version = "v2",
        Description = "The EsensiApp API allows you to manage employee data, salary, and user authentication using JWT and Biometrics.\r\n\r\n" +
        "Use the URL [http://localhost:5000/swagger/index.html](http://localhost:5000/swagger/index.html) to access the API. For local access, use [http://10.0.2.2:5000/](http://10.0.2.2:5000).\r\n\r\n" +
        " **Endpoint to get Image Resources**: [http://localhost:5000/uploads/{username}](http://localhost:5000/swagger/index.html) \r\n\r\n Example : [http://localhost:5000/uploads/DewaPermana.jpg](http://localhost:5000/uploads/DewaPermana.jpg) " +
        " \r\n\r\n ***This Application Programing Interface (API) Development By : Dewa Permana P S***",
    });

    sw.AddSecurityDefinition("FIll the Token!", new OpenApiSecurityScheme
    {
        In = ParameterLocation.Header,
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "Bearer"
    });

    sw.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                     Type = ReferenceType.SecurityScheme,
                     Id = "FIll the Token!"
                }
            },
            Array.Empty<string>()
        },
    });
});
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

/*app.UseStaticFiles(new StaticFileOptions
{
    FileProvider = new PhysicalFileProvider(
        Path.Combine(builder.Environment.WebRootPath, "uploads")
    ),
    RequestPath = "/uploads"
});
*/


app.UseStaticFiles();
app.UseSwagger();
app.UseSwaggerUI();

//app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();
